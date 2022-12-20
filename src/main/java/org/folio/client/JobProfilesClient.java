package org.folio.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
        var profile = getJsonObject("profiles/" + jobProfile.getUrl() + ".json");

        var request = httpWorker.constructPOSTRequest(url, profile.toString());
        var response = httpWorker.sendRequest(request);
        if (response.statusCode() != 201) {
            activateJobProfile(jobProfile, profile);
        }
    }

    public void activateJobProfile(JobProfile jobProfile, ObjectNode profile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();
        ((ObjectNode) profile.get("profile")).put("deleted", false);

        var request = httpWorker.constructPUTRequest(url, profile.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to activate " + jobProfile);
    }

    public void deleteJobProfile(JobProfile jobProfile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();

        var request = httpWorker.constructDELETERequest(url);
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 204, "Failed to delete " + jobProfile);
    }
}
