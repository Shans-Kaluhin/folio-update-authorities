package org.folio.client;

import lombok.SneakyThrows;
import org.folio.model.Configuration;
import org.folio.model.ParsedRecord;
import org.folio.util.HttpWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.folio.util.Mapper.mapResponseToJson;
import static org.folio.util.Mapper.mapToParsedRecords;

public class SRSClient {
    private static final Logger LOG = LoggerFactory.getLogger(SRSClient.class);
    private static final String GET_RECORDS_PATH = "/source-storage/records?recordType=MARC_AUTHORITY&state=ACTUAL&limit=%s&offset=%s";
    private final Configuration configuration;
    private final HttpWorker httpWorker;

    public SRSClient(HttpWorker httpWorker, Configuration configuration) {
        this.configuration = configuration;
        this.httpWorker = httpWorker;
    }


    @SneakyThrows
    private List<ParsedRecord> retrieveRecords(int offset) {
        LOG.info("Retrieving authority records: {} of {}", totalHolders.size(), 10);

        String uri = String.format(GET_RECORDS_PATH, configuration.getDataImportRecordsLimit(), offset);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get records");

        return mapToParsedRecords(response.body());
    }

    @SneakyThrows
    private int retrieveTotalRecords() {
        String uri = String.format(GET_RECORDS_PATH, 0, 0);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get records ids by jobId");

        return mapResponseToJson(response).findValue("totalRecords").asInt();
    }
}
