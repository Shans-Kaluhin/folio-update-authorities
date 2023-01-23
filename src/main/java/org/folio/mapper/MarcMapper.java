package org.folio.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.folio.model.MarcField;
import org.folio.processor.rule.DataSource;
import org.folio.reader.values.CompositeValue;
import org.folio.reader.values.StringValue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MarcMapper {

    public static StringValue mapToStringValue(MarcField marcField) {
        return new StringValue(marcField.getValue(), new DataSource(), null);
    }

    public static CompositeValue mapToCompositeValue(MarcField marcField) {
        CompositeValue compositeValue = new CompositeValue();
        List<StringValue> values = new LinkedList<>();

        values.addAll(mapInd(marcField));
        values.addAll(mapSubfields(marcField));
        compositeValue.addEntry(values);
        return compositeValue;
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
        var subfields = new LinkedList<StringValue>();

        marcField.getSubfields().forEach((subfield, value) -> {
            var dataSource = new DataSource();
            dataSource.setSubfield(subfield.toString());
            var stringValue = new StringValue(value, dataSource, null);
            subfields.add(stringValue);
        });

        return subfields;
    }

    public static List<MarcField> mapRecordFields(JsonNode jsonNode) {
        var mappedFields = new LinkedList<MarcField>();
        var fields = (ArrayNode) jsonNode.get("fields");

        for (var field : fields) {
            field.fields().forEachRemaining(e -> {
                var marcField = mapToMarcField(e.getKey(), e.getValue());
                mappedFields.add(marcField);
            });
        }

        return mappedFields;
    }

    private static MarcField mapToMarcField(String field, JsonNode json) {
        if (json.getNodeType().equals(JsonNodeType.STRING)) {
            return new MarcField(field, json.asText());
        }

        var authoritySubfields = json.get("subfields");
        var ind1 = json.get("ind1").asText();
        var ind2 = json.get("ind2").asText();

        Map<Character, String> subfields = new LinkedHashMap<>();
        for (var authoritySubfield : authoritySubfields) {
            authoritySubfield.fields().forEachRemaining(e -> {
                Character subfield = e.getKey().charAt(0);
                String value = e.getValue().asText();
                subfields.put(subfield, value);
            });
        }

        return new MarcField(field, ind1, ind2, subfields);
    }
}
