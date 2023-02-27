package org.folio.client;

import lombok.extern.slf4j.Slf4j;
import org.folio.model.JobExecution;
import org.folio.util.HttpWorker;

import java.util.List;

import static org.folio.mapper.ResponseMapper.mapResponseToJson;
import static org.folio.mapper.ResponseMapper.mapToFirstExportJobExecution;
import static org.folio.model.enums.JobProfile.EXPORT_JOB_PROFILE;

@Slf4j
public class DataExportClient {
    private static final String UPLOAD_DEFINITION_BODY = "{ " +
            "    \"jobProfileId\": \"%s\"," +
            "    \"recordType\": \"AUTHORITY\"," +
            "    \"type\": \"uuid\"," +
            "    \"uuids\": %s" +
            "}";
    private static final String QUICK_EXPORT_PATH = "/data-export/quick-export";
    private static final String DATA_EXPORT_JOB_PATH = "/data-export/job-executions?query=id=%s";
    private static final String DATA_EXPORT_FILE_PATH = "/data-export/job-executions/%s/download/%s";
    private final HttpWorker httpWorker;

    public DataExportClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public String exportIds(List<String> ids) {
        String body = String.format(UPLOAD_DEFINITION_BODY, EXPORT_JOB_PROFILE.getId(), ids.toString());

        var request = httpWorker.constructPOSTRequest(QUICK_EXPORT_PATH, body);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to export inventory ids");

        return mapResponseToJson(response).get("jobExecutionId").asText();
    }

    public JobExecution retrieveJobExecution(String jobId) {
        var uri = String.format(DATA_EXPORT_JOB_PATH, jobId);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get export job");

        return mapToFirstExportJobExecution(response.body());
    }

    public String retrieveJobExecutionFile(JobExecution jobExecution) {
        var uri = String.format(DATA_EXPORT_FILE_PATH, jobExecution.getId(), jobExecution.getFileId());

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get exported file link");

        var link = mapResponseToJson(response).get("link").asText();

        return retrieveFile(link);
    }

    public String retrieveFile(String link) {
        var request = httpWorker.constructExternalRequest(link);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to download exported file");

        return response.body();
    }
}
