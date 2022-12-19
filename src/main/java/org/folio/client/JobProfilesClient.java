package org.folio.client;

import org.folio.model.enums.JobProfile;
import org.folio.util.HttpWorker;

import static org.folio.util.FileWorker.getJsonObject;


public class JobProfilesClient {
    private static final String DATA_IMPORT_PROFILES_PATH = "/data-import-profiles/";
    private final HttpWorker httpWorker;

    public JobProfilesClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public void createJobProfile(JobProfile jobProfile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl();
        var profile = getJsonObject(jobProfile.getUrl() + ".json");

        var request = httpWorker.constructPOSTRequest(url, profile.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 201, "Failed to create " + jobProfile);
    }

    public void deleteJobProfile(JobProfile jobProfile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();

        var request = httpWorker.constructDELETERequest(url);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 204, "Failed to delete " + jobProfile);
    }
}
