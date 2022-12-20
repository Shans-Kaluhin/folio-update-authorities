package org.folio.service;

import org.folio.client.JobProfilesClient;
import org.folio.client.SRSClient;
import org.folio.model.enums.JobProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobProfileService {
    private static final Logger LOG = LoggerFactory.getLogger(SRSClient.class);
    private final JobProfilesClient client;

    public JobProfileService(JobProfilesClient client) {
        this.client = client;
    }

    public void populateProfiles() {
        LOG.info("Populating default job profiles...");
        client.createJobProfile(JobProfile.MATCH_PROFILE);
        client.createJobProfile(JobProfile.MAPPING_PROFILE);
        client.createJobProfile(JobProfile.ACTION_PROFILE);
        client.createJobProfile(JobProfile.JOB_PROFILE);
    }

    public void deleteProfiles() {
        LOG.info("Deleting default job profiles...");
        client.deleteJobProfile(JobProfile.JOB_PROFILE);
        client.deleteJobProfile(JobProfile.MATCH_PROFILE);
        client.deleteJobProfile(JobProfile.ACTION_PROFILE);
        client.deleteJobProfile(JobProfile.MAPPING_PROFILE);
    }
}
