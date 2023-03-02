package org.folio.client;

import lombok.extern.slf4j.Slf4j;
import org.folio.model.Configuration;
import org.folio.util.HttpWorker;

import java.util.ArrayList;
import java.util.List;

import static org.folio.mapper.ResponseMapper.mapResponseToJson;
import static org.folio.mapper.ResponseMapper.mapToIds;

@Slf4j
public class InventoryClient {
    private static final String GET_RECORDS_PATH = "/authority-storage/authorities?offset=%s&limit=%s&query=id==*+sortBy+id";
    private final HttpWorker httpWorker;

    public InventoryClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public List<String> retrieveIdsPartitionaly(Configuration configuration, int total) {
        var result = new ArrayList<String>();
        var offset = configuration.getOffset();

        while (result.size() < configuration.getImportLimit() && offset < total) {
            var records = retrieveIds(configuration.getInventoryLimit(), offset, total);
            if (records.isEmpty()) {
                log.info("Inventory storage returned empty result");
                break;
            }
            result.addAll(records);
            offset += records.size();
        }
        return result;
    }

    public List<String> retrieveIds(int limit, int offset, int total) {
        int retrieveTo = Math.min(total, offset + limit);
        var uri = String.format(GET_RECORDS_PATH, offset, limit);
        log.info("Retrieving inventory records from {} to {}...", offset, retrieveTo);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get inventory records");

        return mapToIds(response.body());
    }

    public int retrieveTotalRecords() {
        String uri = String.format(GET_RECORDS_PATH, 0, 0);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get inventory total records");

        return mapResponseToJson(response).findValue("totalRecords").asInt();
    }
}
