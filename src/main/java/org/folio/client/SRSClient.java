package org.folio.client;

import org.folio.model.ParsedRecord;
import org.folio.util.HttpWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.folio.util.Mapper.mapResponseToJson;
import static org.folio.util.Mapper.mapToParsedRecords;

public class SRSClient {
    private static final Logger LOG = LoggerFactory.getLogger(SRSClient.class);
    private static final String GET_RECORDS_PATH = "/source-storage/records?recordType=MARC_AUTHORITY&state=ACTUAL&limit=%s&offset=%s";
    private final HttpWorker httpWorker;

    public SRSClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public List<ParsedRecord> retrieveRecords(int limit, int offset) {
        LOG.info("Retrieving records from {} to {}", offset, offset + limit);
        String uri = String.format(GET_RECORDS_PATH, limit, offset);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get records");

        return mapToParsedRecords(response.body());
    }

    public int retrieveTotalRecords() {
        String uri = String.format(GET_RECORDS_PATH, 0, 0);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get total records");

        return mapResponseToJson(response).findValue("totalRecords").asInt();
    }
}
