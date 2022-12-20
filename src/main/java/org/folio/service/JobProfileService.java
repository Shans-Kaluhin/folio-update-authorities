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

    public void populateProfiles() {
        log.info("Populating default job profiles...");
        client.createJobProfile(JobProfile.MATCH_PROFILE);
        client.createJobProfile(JobProfile.MAPPING_PROFILE);
        client.createJobProfile(JobProfile.ACTION_PROFILE);
        client.createJobProfile(JobProfile.JOB_PROFILE);
    }

    public void deleteProfiles() {
        log.info("Deleting default job profiles...");
        client.deleteJobProfile(JobProfile.JOB_PROFILE);
        client.deleteJobProfile(JobProfile.MATCH_PROFILE);
        client.deleteJobProfile(JobProfile.ACTION_PROFILE);
        client.deleteJobProfile(JobProfile.MAPPING_PROFILE);
    }
}
