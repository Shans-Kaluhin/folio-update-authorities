package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import org.folio.client.JobProfilesClient;
import org.folio.model.enums.JobProfile;

@Slf4j
public class JobProfileService {
    private final JobProfilesClient client;

    public JobProfileService(JobProfilesClient client) {
        this.client = client;
    }

    public void populateExportProfiles() {
        log.info("Populating default export job profiles...");
        client.createExportJobProfile(JobProfile.EXPORT_MAPPING_PROFILE);
        client.createExportJobProfile(JobProfile.EXPORT_JOB_PROFILE);
    }

    public void populateImportProfiles() {
        log.info("Populating default import job profiles...");
        client.createImportJobProfile(JobProfile.IMPORT_MATCH_PROFILE);
        client.createImportJobProfile(JobProfile.IMPORT_MAPPING_PROFILE);
        client.createImportJobProfile(JobProfile.IMPORT_ACTION_PROFILE);
        client.createImportJobProfile(JobProfile.IMPORT_JOB_PROFILE);
    }

    public void deleteImportProfiles() {
        log.info("Deleting default import job profiles...");
        client.deleteJobProfile(JobProfile.IMPORT_JOB_PROFILE);
        client.deleteJobProfile(JobProfile.IMPORT_MATCH_PROFILE);
        client.deleteJobProfile(JobProfile.IMPORT_ACTION_PROFILE);
        client.deleteJobProfile(JobProfile.IMPORT_MAPPING_PROFILE);
    }
}
