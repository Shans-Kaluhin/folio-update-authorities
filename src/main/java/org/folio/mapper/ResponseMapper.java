package org.folio.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.folio.model.JobExecution;
import org.folio.model.ParsedRecord;
import org.folio.model.UploadDefinition;
import org.folio.model.enums.JobStatus;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.folio.mapper.MarcMapper.mapRecordFields;

@Slf4j
public class ResponseMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @SneakyThrows
    public static JsonNode mapResponseToJson(HttpResponse<String> response) {
        return OBJECT_MAPPER.readTree(response.body());
    }

    @SneakyThrows
    public static JobExecution mapToJobExecutionById(String json, String jobId) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        for (var job : jobs) {
            var id = job.get("id").asText();
            if (jobId.equals(id)) {
                return mapToJobExecution(job);
            }
        }
       return mapToEmptyJobExecution();
    }

    @SneakyThrows
    public static JobExecution mapToFirstJobExecution(String json) {
        var jobs = OBJECT_MAPPER.readTree(json).get("jobExecutions");

        if (jobs.has(0)) {
            return mapToJobExecution(jobs.get(0));
        }
        return mapToEmptyJobExecution();
    }

    public static JobExecution mapToJobExecution(JsonNode job) {
            var id = job.get("id").asText();
            var progress = job.get("progress");
            var status = job.get("status").asText();
            var uiStatus = job.get("uiStatus").asText();
            var current = progress.get("current").asInt();
            var total = progress.get("total").asInt();

            return new JobExecution(id, status, uiStatus, current, total);
    }

    public static JobExecution mapToEmptyJobExecution() {
        return new JobExecution(null, JobStatus.NOT_FOUND.name(), "INITIALIZING", 0, 0);
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
        var jsonRecords = OBJECT_MAPPER.readTree(json).get("records");

        var records = new ArrayList<ParsedRecord>();
        for (JsonNode record : jsonRecords) {
            records.add(mapToParsedRecord(record));
        }

        return records;
    }

    private static ParsedRecord mapToParsedRecord(JsonNode jsonNode) {
        var id = jsonNode.get("id").asText();

        var parsedRecord = jsonNode.get("parsedRecord");
        if (parsedRecord == null) {
            return null;
        }

        var content = parsedRecord.get("content");
        var leader = content.get("leader").asText();
        var fields = mapRecordFields(content);
        var externalId = jsonNode.get("externalIdsHolder").get("authorityId").asText();

        return new ParsedRecord(id, leader, externalId, fields);
    }
}
