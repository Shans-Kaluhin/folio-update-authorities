package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import org.folio.model.ParsedRecord;
import org.folio.util.MarcRecordWriter;
import org.folio.writer.RecordWriter;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.folio.mapper.MarcMapper.mapToCompositeValue;
import static org.folio.mapper.MarcMapper.mapToStringValue;
import static org.folio.util.FileWorker.writeFile;

@Slf4j
@Service
public class MarcConverterService {
    private static final String FILE_NAME = "Release Upgrade - Migrate MARC authority records.mrc";

    public Path writeRecords(List<ParsedRecord> records) {
        log.info("Generating authorities mrc file...");
        return writeFile(FILE_NAME, records.stream()
                .filter(Objects::nonNull)
                .map(this::convertRecord)
                .toList());
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
