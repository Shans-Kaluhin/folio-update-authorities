package org.folio.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.model.JobExecution;
import org.folio.model.ParsedRecord;
import org.folio.model.UploadDefinition;
import org.folio.model.enums.JobStatus;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.folio.mapper.MarcMapper.mapRecordFields;

public class ResponseMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @SneakyThrows
    public static JsonNode mapResponseToJson(HttpResponse<String> response) {
        return OBJECT_MAPPER.readTree(response.body());
    }

    @SneakyThrows
    public static JobExecution mapToJobExecution(String json, String jobId) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        for (var job : jobs) {
            var id = job.get("id").asText();

            if (jobId.equals(id)) {
                var progress = job.get("progress");
                var status = job.get("status").asText();
                var uiStatus = job.get("uiStatus").asText();
                var current = progress.get("current").asInt();
                var total = progress.get("total").asInt();

                return new JobExecution(status, uiStatus, current, total);
            }
        }
        return new JobExecution(JobStatus.ERROR.name(), "JOB_NOT_FOUND", 0, 0);
    }

    @SneakyThrows
    public static UploadDefinition mapUploadDefinition(String json, Path filePath) {
        var jsonBody = OBJECT_MAPPER.readTree(json);

        var fileDefinitions = jsonBody.findValue("fileDefinitions");
        var uploadDefinitionId = fileDefinitions.findValue("uploadDefinitionId").asText();
        var jobExecutionId = fileDefinitions.findValue("jobExecutionId").asText();
        var fileId = fileDefinitions.findValue("id").asText();

        return new UploadDefinition(uploadDefinitionId, jobExecutionId, fileId, filePath);
    }

    @SneakyThrows
    public static List<ParsedRecord> mapToParsedRecords(String json) {
        var records = new ArrayList<ParsedRecord>();
        var jsonRecords = OBJECT_MAPPER.readTree(json).get("records");

        for (JsonNode record : jsonRecords) {
            records.add(mapToParsedRecord(record));
        }

        return records;
    }

    private static ParsedRecord mapToParsedRecord(JsonNode jsonNode) {
        var parsedRecord = jsonNode.get("parsedRecord");
        var content = parsedRecord.get("content");

        var id = parsedRecord.get("id").asText();
        var leader = content.get("leader").asText();
        var fields = mapRecordFields(content);

        return new ParsedRecord(id, leader, fields);
    }
}
