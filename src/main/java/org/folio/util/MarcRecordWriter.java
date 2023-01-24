package org.folio.util;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.processor.translations.Translation;
import org.folio.writer.fields.RecordControlField;
import org.folio.writer.fields.RecordDataField;
import org.folio.writer.impl.AbstractRecordWriter;
import org.marc4j.Constants;
import org.marc4j.MarcException;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.marc4j.util.CustomDecimalFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static org.marc4j.MarcStreamWriter.ENCODING_FOR_DIR_ENTRIES;

public class MarcRecordWriter extends AbstractRecordWriter {
    private static final DecimalFormat format4Use = new CustomDecimalFormat(4);
    private static final DecimalFormat format5Use = new CustomDecimalFormat(5);
    private static final Charset encoding = StandardCharsets.UTF_8;
    private final boolean allowOversizeEntry = false;
    private boolean hasOversizeOffset = false;
    private boolean hasOversizeLength = false;
    private final MarcFactory factory;
    private final Record record;

    public MarcRecordWriter(String leader) {
        this.factory = new MarcFactoryImpl();
        this.record = this.factory.newRecord(leader);
    }

    public List<VariableField> getFields() {
        return this.record.getVariableFields();
    }

    public void writeLeader(Translation translation) {
        //Use constructor
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
        if (CollectionUtils.isEmpty(getFields())) {
            return "";
        }

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            final ByteArrayOutputStream dir = new ByteArrayOutputStream();
            hasOversizeOffset = false;
            hasOversizeLength = false;
            int previous = 0;

            // control fields
            for (final ControlField cf : record.getControlFields()) {
                data.write(getDataElement(cf.getData()));
                data.write(Constants.FT);
                dir.write(getEntry(cf.getTag(), data.size() - previous, previous));
                previous = data.size();
            }

            // data fields
            for (final DataField df : record.getDataFields()) {
                data.write(df.getIndicator1());
                data.write(df.getIndicator2());
                for (final Subfield sf : df.getSubfields()) {
                    data.write(Constants.US);
                    data.write(sf.getCode());
                    data.write(getDataElement(sf.getData()));
                }
                data.write(Constants.FT);
                dir.write(getEntry(df.getTag(), data.size() - previous, previous));
                previous = data.size();
            }
            dir.write(Constants.FT);

            // base address of data and logical record length
            final Leader ldr = record.getLeader();

            final int baseAddress = 24 + dir.size();
            ldr.setBaseAddressOfData(baseAddress);
            final int recordLength = ldr.getBaseAddressOfData() + data.size() + 1;
            ldr.setRecordLength(recordLength);

            // write record to output stream
            dir.close();
            data.close();

            if (!allowOversizeEntry && (baseAddress > 99999 || recordLength > 99999 || hasOversizeOffset)) {
                throw new MarcException("Record is too long to be a valid MARC binary record, it's length would be " +
                        recordLength + " which is more that 99999 bytes");
            }
            if (!allowOversizeEntry && (hasOversizeLength)) {
                throw new MarcException("Record has field that is too long to be a valid MARC binary record. "
                        + "The maximum length for a field counting all of the sub-fields is 9999 bytes.");
            }
            writeLeader(out, ldr);
            out.write(dir.toByteArray());
            out.write(data.toByteArray());
            out.write(Constants.RT);
            out.close();

            return out.toString(encoding);
        } catch (final IOException e) {
            throw new MarcException("IO Error occured while writing record", e);
        }
    }

    private void writeLeader(OutputStream out, Leader ldr) throws IOException {
        String leaderEncoding = ENCODING_FOR_DIR_ENTRIES;
        out.write(format5Use.format(ldr.getRecordLength()).getBytes(leaderEncoding));
        out.write(ldr.getRecordStatus());
        out.write(ldr.getTypeOfRecord());
        out.write(new String(ldr.getImplDefined1()).getBytes(leaderEncoding));
        out.write(ldr.getCharCodingScheme());
        out.write(Integer.toString(ldr.getIndicatorCount()).getBytes(leaderEncoding));
        out.write(Integer.toString(ldr.getSubfieldCodeLength()).getBytes(leaderEncoding));
        out.write(format5Use.format(ldr.getBaseAddressOfData()).getBytes(leaderEncoding));
        out.write(new String(ldr.getImplDefined2()).getBytes(leaderEncoding));
        out.write(new String(ldr.getEntryMap()).getBytes(leaderEncoding));
    }

    private byte[] getEntry(String tag, int length, int start) throws IOException {
        final String entryUse = tag + format4Use.format(length) + format5Use.format(start);
        if (length > 99999) {
            hasOversizeLength = true;
        }
        if (start > 99999) {
            hasOversizeOffset = true;
        }
        return (entryUse.getBytes(ENCODING_FOR_DIR_ENTRIES));
    }

    private byte[] getDataElement(String data) throws IOException {
        return data.getBytes(encoding);
    }
}