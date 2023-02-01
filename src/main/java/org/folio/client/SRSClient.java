package org.folio.client;

import lombok.extern.slf4j.Slf4j;
import org.folio.model.ParsedRecord;
import org.folio.util.HttpWorker;

import java.util.ArrayList;
import java.util.List;

import static org.folio.mapper.ResponseMapper.mapResponseToJson;
import static org.folio.mapper.ResponseMapper.mapToParsedRecords;

@Slf4j
public class SRSClient {
    private static final String PARAMS = "?recordType=MARC_AUTHORITY&state=ACTUAL&orderBy=matched_id,ASC&limit=%s&offset=%s";
    private static final String GET_RECORDS_PATH = "/source-storage/records" + PARAMS;
    private static final int SRS_LIMIT = 10000;
    private final HttpWorker httpWorker;

    public SRSClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public List<ParsedRecord> retrieveRecordsPartitionaly(int limit, int offset, int total) {
        if (limit < SRS_LIMIT) {
            return retrieveRecords(limit, offset, total);
        }

        var result = new ArrayList<ParsedRecord>();
        while (result.size() < limit && offset < total) {
            var records = retrieveRecords(SRS_LIMIT, offset, total);
            result.addAll(records);
            offset += records.size();
        }
        return result;
    }

    public List<ParsedRecord> retrieveRecords(int limit, int offset, int total) {
        int retrieveTo = Math.min(total, offset + limit);
        log.info("Retrieving srs records from {} to {}...", offset, retrieveTo);
        String uri = String.format(GET_RECORDS_PATH, limit, offset);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get srs records");

        return mapToParsedRecords(response.body());
    }

    public int retrieveTotalRecords() {
        String uri = String.format(GET_RECORDS_PATH, 0, 0);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get srs total records");

        return mapResponseToJson(response).findValue("totalRecords").asInt();
    }
}
