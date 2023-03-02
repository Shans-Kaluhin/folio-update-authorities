package org.folio.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.folio.model.ExportJobExecution;
import org.folio.model.ImportJobExecution;
import org.folio.model.UploadDefinition;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.folio.model.enums.JobStatus.INITIALIZING;
import static org.folio.model.enums.JobStatus.NOT_FOUND;

@Slf4j
public class ResponseMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public static JsonNode mapResponseToJson(HttpResponse<String> response) {
        return OBJECT_MAPPER.readTree(response.body());
    }

    @SneakyThrows
    public static ImportJobExecution mapToJobExecutionById(String json, String jobId) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        for (var job : jobs) {
            var id = job.get("id").asText();
            if (jobId.equals(id)) {
                return mapToImportJobExecution(job);
            }
        }
        return mapToEmptyImportJobExecution();
    }

    @SneakyThrows
    public static ImportJobExecution mapToFirstImportJobExecution(String json) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        if (jobs.has(0)) {
            return mapToImportJobExecution(jobs.get(0));
        }
        return mapToEmptyImportJobExecution();
    }

    @SneakyThrows
    public static ExportJobExecution mapToFirstExportJobExecution(String json) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        if (jobs.has(0)) {
            return mapToExportJobExecution(jobs.get(0));
        }
        return mapToEmptyExportJobExecution();
    }

    public static ExportJobExecution mapToExportJobExecution(JsonNode job) {
        var id = job.get("id").asText();
        var progress = job.get("progress");
        var files = job.get("exportedFiles");
        var status = job.get("status").asText();
        var exported = progress.get("exported").asInt();
        var failed = progress.get("failed").asInt();
        var total = progress.get("total").asInt();

        String fileId = null;
        if (files != null && files.size() != 0) {
            fileId = files.get(0).get("fileId").asText();
        }

        return new ExportJobExecution(id, status, fileId, total, failed, exported);
    }

    public static ImportJobExecution mapToImportJobExecution(JsonNode job) {
        var id = job.get("id").asText();
        var progress = job.get("progress");
        var status = job.get("status").asText();
        var uiStatus = job.get("uiStatus").asText();
        var current = progress.get("current").asInt();
        var total = progress.get("total").asInt();

        return new ImportJobExecution(id, status, uiStatus, total, current);
    }

    public static ImportJobExecution mapToEmptyImportJobExecution() {
        return new ImportJobExecution(null, NOT_FOUND.name(), INITIALIZING.name(), 0, 0);
    }

    public static ExportJobExecution mapToEmptyExportJobExecution() {
        return new ExportJobExecution(null, NOT_FOUND.name(), INITIALIZING.name(), 0, 0, 0);
    }

    @SneakyThrows
    public static UploadDefinition mapUploadDefinition(String json, String fileBody) {
        var jsonBody = OBJECT_MAPPER.readTree(json);

        var fileDefinitions = jsonBody.findValue("fileDefinitions");
        var uploadDefinitionId = fileDefinitions.findValue("uploadDefinitionId").asText();
        var jobExecutionId = fileDefinitions.findValue("jobExecutionId").asText();
        var fileId = fileDefinitions.findValue("id").asText();

        return new UploadDefinition(uploadDefinitionId, jobExecutionId, fileId, fileBody);
    }

    @SneakyThrows
    public static List<String> mapToIds(String json) {
        var jsonRecords = OBJECT_MAPPER.readTree(json).get("authorities");

        var records = new ArrayList<String>();
        for (JsonNode record : jsonRecords) {
            var id = '"' + record.get("id").asText() + '"';
            records.add(id);
        }

        return records;
    }
}
