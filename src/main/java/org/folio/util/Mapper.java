package org.folio.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.SneakyThrows;
import org.folio.model.MarcField;
import org.folio.model.ParsedRecord;
import org.folio.model.UploadDefinition;
import org.folio.processor.rule.DataSource;
import org.folio.reader.values.CompositeValue;
import org.folio.reader.values.StringValue;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public static JsonNode mapResponseToJson(HttpResponse<String> response) {
        return OBJECT_MAPPER.readTree(response.body());
    }

    public static CompositeValue mapToCompositeValue(MarcField marcField) {
        CompositeValue compositeValue = new CompositeValue();
        List<StringValue> values = new ArrayList<>();

        values.addAll(mapInd(marcField));
        values.addAll(mapSubfields(marcField));
        compositeValue.addEntry(values);
        return compositeValue;
    }

    public static StringValue mapToStringValue(MarcField marcField) {
        return new StringValue(marcField.getValue(), new DataSource(), null);
    }

    public static List<StringValue> mapInd(MarcField marcField) {
        var d1 = new DataSource();
        d1.setIndicator("1");
        var ind1 = new StringValue(marcField.getInd1(), d1, null);

        var d2 = new DataSource();
        d2.setIndicator("2");
        var ind2 = new StringValue(marcField.getInd2(), d2, null);

        return List.of(ind1, ind2);
    }

    public static List<StringValue> mapSubfields(MarcField marcField) {
        var subfields = new ArrayList<StringValue>();

        marcField.getSubfields().forEach((subfield, value) -> {
            var dataSource = new DataSource();
            dataSource.setSubfield(subfield.toString());
            var stringValue = new StringValue(value, dataSource, null);
            subfields.add(stringValue);
        });

        return subfields;
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

    private static List<MarcField> mapRecordFields(JsonNode jsonNode) {
        var mappedFields = new ArrayList<MarcField>();
        var fields = (ArrayNode) jsonNode.get("fields");

        for (var field : fields) {
            field.fields().forEachRemaining(e -> {
                var marcField = mapToMarcField(e.getKey(), e.getValue());
                mappedFields.add(marcField);
            });
        }

        return mappedFields;
    }

    public static MarcField mapToMarcField(String field, JsonNode value) {
        if (value.getNodeType().equals(JsonNodeType.STRING)) {
            return new MarcField(field, value.asText());
        }

        var authoritySubfields = value.get("subfields");
        var ind1 = value.get("ind1").asText();
        var ind2 = value.get("ind2").asText();

        Map<Character, String> subfields = new HashMap<>();
        for (var authoritySubfield : authoritySubfields) {
            authoritySubfield.fields().forEachRemaining(e -> {
                Character subfield = e.getKey().charAt(0);
                if (!(subfield.equals('0') || subfield.equals('9'))) {
                    subfields.put(subfield, e.getValue().asText());
                }
            });
        }

        return new MarcField(field, ind1, ind2, subfields);
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
}
