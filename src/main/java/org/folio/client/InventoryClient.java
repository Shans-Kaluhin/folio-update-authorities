package org.folio.client;

import lombok.extern.slf4j.Slf4j;
import org.folio.util.HttpWorker;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithError;

@Slf4j
public class InventoryClient {
    private static final String GET_AUTHORITY_RECORD_PATH = "/authority-storage/authorities/%s";
    private final HttpWorker httpWorker;

    public InventoryClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public boolean isAuthorityExist(String id) {
        String uri = String.format(GET_AUTHORITY_RECORD_PATH, id);

        var request = httpWorker.constructGETRequest(uri);
        var response = httpWorker.sendRequest(request);

        if (response.statusCode() == 200) {
            return true;
        } else if (response.statusCode() == 404) {
            return false;
        }else {
            exitWithError("Failed to get inventory records. Response: " + response.body());
            return false;
        }
    }
}
