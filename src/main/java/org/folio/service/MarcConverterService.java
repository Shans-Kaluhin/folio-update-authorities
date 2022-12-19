package org.folio.service;

import org.folio.model.ParsedRecord;
import org.folio.util.MarcRecordWriter;
import org.folio.writer.RecordWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static org.folio.util.FileWorker.writeFile;
import static org.folio.util.Mapper.mapToCompositeValue;
import static org.folio.util.Mapper.mapToStringValue;

public class MarcConverterService {
    private static final Logger LOG = LoggerFactory.getLogger(MarcConverterService.class);
    private static final String FILE_NAME = "Release Upgrade - Migrate MARC authority records.mrc";

    public Path writeRecords(List<ParsedRecord> records) {
        LOG.info("Generating mrc file...");

        var mrcFile = records.stream()
                .map(this::convertRecord)
                .toList();

        return writeFile(FILE_NAME, mrcFile);
    }

    private String convertRecord(ParsedRecord parsedRecord) {
        RecordWriter recordWriter = new MarcRecordWriter(parsedRecord.getLeader());
        parsedRecord.getFields().forEach(marcField -> {
            if (marcField.getValue() != null) {
                recordWriter.writeField(marcField.getTag(), mapToStringValue(marcField));
            } else {
                recordWriter.writeField(marcField.getTag(), mapToCompositeValue(marcField));
            }
        });

        return recordWriter.getResult();
    }
}
