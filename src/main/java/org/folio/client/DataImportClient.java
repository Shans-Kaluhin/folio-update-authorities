package org.folio.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.folio.model.JobExecution;
import org.folio.model.UploadDefinition;
import org.folio.util.HttpWorker;

import static org.folio.mapper.ResponseMapper.mapResponseToJson;
import static org.folio.mapper.ResponseMapper.mapToFirstImportJobExecution;
import static org.folio.mapper.ResponseMapper.mapToJobExecutionById;
import static org.folio.mapper.ResponseMapper.mapUploadDefinition;
import static org.folio.model.enums.JobProfile.JOB_PROFILE;
import static org.folio.util.FileWorker.getJsonObject;

public class DataImportClient {
    private static final String UPLOAD_DEFINITION_BODY = "{\"fileDefinitions\":[{\"size\": 1,\"name\": \"%s\"}]}";
    private static final String UPLOAD_DEFINITION_PATH = "/data-import/uploadDefinitions";
    private static final String UPLOAD_DEFINITION_BY_ID_PATH = "/data-import/uploadDefinitions/%s";
    private static final String UPLOAD_FILE_PATH = UPLOAD_DEFINITION_PATH + "/%s/files/%s";
    private static final String UPLOAD_JOB_PROFILE_PATH = UPLOAD_DEFINITION_PATH + "/%s/processFiles?defaultMapping=false";
    private static final String JOB_EXECUTION_PATH = "/metadata-provider/jobExecutions?profileIdAny=%s&sortBy=completed_date,desc";
    private static final String ANY_JOB_IN_PROGRESS_PATH = "/metadata-provider/jobExecutions?statusAny=PARSING_IN_PROGRESS&sortBy=completed_date,desc";
    private static final String FILE_NAME = "Release Upgrade - Migrate MARC authority records.mrc";

    private final HttpWorker httpWorker;

    public DataImportClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public UploadDefinition uploadDefinition(String fileBody) {
        String body = String.format(UPLOAD_DEFINITION_BODY, FILE_NAME);

        var request = httpWorker.constructPOSTRequest(UPLOAD_DEFINITION_PATH, body);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 201, "Failed to upload definition");

        return mapUploadDefinition(response.body(), fileBody);
    }

    public JsonNode retrieveUploadDefinition(String uploadDefinitionId) {
        var uri = String.format(UPLOAD_DEFINITION_BY_ID_PATH, uploadDefinitionId);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get upload definition");

        return mapResponseToJson(response);
    }

    public void uploadFile(UploadDefinition uploadDefinition) {
        var uploadPath = String.format(UPLOAD_FILE_PATH, uploadDefinition.getUploadDefinitionId(), uploadDefinition.getFileId());

        var request = httpWorker.constructFilePOSTRequest(uploadPath, uploadDefinition.getFileBody());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to upload file");
    }

    public void uploadJobProfile(UploadDefinition uploadDefinition, String jobName) {
        var uploadPath = String.format(UPLOAD_JOB_PROFILE_PATH, uploadDefinition.getUploadDefinitionId());
        var jobProfile = getJsonObject(jobName);

        jobProfile.set("uploadDefinition", retrieveUploadDefinition(uploadDefinition.getUploadDefinitionId()));

        var request = httpWorker.constructPOSTRequest(uploadPath, jobProfile.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 204, "Failed to upload job profile");
    }

    public JobExecution retrieveJobExecution(String jobId) {
        var getJobStatusPath = String.format(JOB_EXECUTION_PATH, JOB_PROFILE.getId());

        var request = httpWorker.constructGETRequest(getJobStatusPath);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to fetching jo status");

        return mapToJobExecutionById(response.body(), jobId);
    }

    public JobExecution retrieveFirstInProgressJob() {
        var request = httpWorker.constructGETRequest(ANY_JOB_IN_PROGRESS_PATH);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to fetching jo status");

        return mapToFirstImportJobExecution(response.body());
    }
}
