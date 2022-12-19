package org.folio.service;

import org.folio.client.JobProfilesClient;
import org.folio.model.enums.JobProfile;

public class JobProfileService {
    private final JobProfilesClient client;

    public JobProfileService(JobProfilesClient client) {
        this.client = client;
    }

    public void populateProfiles() {
        client.createJobProfile(JobProfile.MATCHING_PROFILE);
        client.createJobProfile(JobProfile.MAPPING_PROFILE);
        client.createJobProfile(JobProfile.ACTION_PROFILE);
        client.createJobProfile(JobProfile.JOB_PROFILE);
    }

    public void deleteProfiles() {
        client.deleteJobProfile(JobProfile.JOB_PROFILE);
        client.deleteJobProfile(JobProfile.ACTION_PROFILE);
        client.deleteJobProfile(JobProfile.MAPPING_PROFILE);
        client.deleteJobProfile(JobProfile.MATCHING_PROFILE);
    }
}
