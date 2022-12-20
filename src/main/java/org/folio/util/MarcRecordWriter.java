package org.folio.util;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.processor.translations.Translation;
import org.folio.writer.fields.RecordControlField;
import org.folio.writer.fields.RecordDataField;
import org.folio.writer.impl.AbstractRecordWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SortedMarcFactoryImpl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MarcRecordWriter extends AbstractRecordWriter {
    private final MarcFactory factory;
    protected final String encoding;
    protected final Record record;

    public MarcRecordWriter(String leader) {
        this.encoding = StandardCharsets.UTF_8.name();
        this.factory = new SortedMarcFactoryImpl();
        this.record = this.factory.newRecord(leader);
    }

    public void writeLeader(Translation translation) {
        if (translation.getFunction().equals("set_17-19_positions")) {
            char[] implDefined2 = new char[]{translation.getParameter("position17").charAt(0), translation.getParameter("position18").charAt(0), translation.getParameter("position19").charAt(0)};
            this.record.getLeader().setImplDefined2(implDefined2);
        }
    }

    public void writeControlField(RecordControlField recordControlField) {
        ControlField marcControlField = this.factory.newControlField(recordControlField.getTag(), recordControlField.getData());
        this.record.addVariableField(marcControlField);
    }

    public void writeDataField(RecordDataField recordDataField) {
        DataField marcDataField = this.factory.newDataField(recordDataField.getTag(), recordDataField.getIndicator1(), recordDataField.getIndicator2());

        for (Map.Entry<Character, String> subField : recordDataField.getSubFields()) {
            Character subFieldCode = subField.getKey();
            String subFieldData = subField.getValue();
            marcDataField.addSubfield(this.factory.newSubfield(subFieldCode, subFieldData));
        }

        this.record.addVariableField(marcDataField);
    }

    public String getResult() {
        OutputStream outputStream = new ByteArrayOutputStream();
        MarcWriter writer = new MarcStreamWriter(outputStream, this.encoding);
        if (CollectionUtils.isNotEmpty(this.getFields())) {
            writer.write(this.record);
            writer.close();
            return outputStream.toString();
        } else {
            writer.close();
            return "";
        }
    }

    public List<VariableField> getFields() {
        return this.record.getVariableFields();
    }
}