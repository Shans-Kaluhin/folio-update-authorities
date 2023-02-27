package org.folio.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.folio.model.enums.JobProfile;
import org.folio.util.HttpWorker;

import static org.folio.util.FileWorker.getJsonObject;

public class JobProfilesClient {
    private static final String DATA_IMPORT_PROFILES_PATH = "/data-import-profiles/";
    private static final String DATA_EXPORT_PROFILES_PATH = "/data-export/";
    private final HttpWorker httpWorker;

    public JobProfilesClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public void createExportJobProfile(JobProfile jobProfile) {
        var url = DATA_EXPORT_PROFILES_PATH + jobProfile.getUrl();
        var profile = getJsonObject("exportProfiles/" + jobProfile.getUrl() + ".json");

        var request = httpWorker.constructPOSTRequest(url, profile.toString());
        httpWorker.sendRequest(request);
    }

    public void createImportJobProfile(JobProfile jobProfile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl();
        var profile = getJsonObject("importProfiles/" + jobProfile.getUrl() + ".json");

        var request = httpWorker.constructPOSTRequest(url, profile.toString());
        var response = httpWorker.sendRequest(request);
        if (response.statusCode() == 422) {
            deleteRelationsJobProfile(jobProfile, profile);
            activateJobProfile(jobProfile, profile);
        }
    }

    public void deleteJobProfile(JobProfile jobProfile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();

        var request = httpWorker.constructDELETERequest(url);
        httpWorker.sendRequest(request);
    }

    private void activateJobProfile(JobProfile jobProfile, ObjectNode profile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();
        ((ObjectNode) profile.get("profile")).put("deleted", false);

        var request = httpWorker.constructPUTRequest(url, profile.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to activate " + jobProfile);
    }

    private void deleteRelationsJobProfile(JobProfile jobProfile, ObjectNode profile) {
        var url = DATA_IMPORT_PROFILES_PATH + jobProfile.getUrl() + '/' + jobProfile.getId();

        var requestBody = profile.deepCopy();
        var existRelations = requestBody.remove("addedRelations");
        requestBody.put("deletedRelations", existRelations);

        var request = httpWorker.constructPUTRequest(url, requestBody.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to remove relations " + jobProfile);
    }
}
