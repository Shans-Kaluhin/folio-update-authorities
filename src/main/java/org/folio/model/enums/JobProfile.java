package org.folio.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobProfile {
    IMPORT_JOB_PROFILE("a2db814f-1106-4b08-90e0-39e2647e5438", "jobProfiles"),
    IMPORT_MATCH_PROFILE("ed99eff1-a008-47ff-b2e3-763315a01d9e", "matchProfiles"),
    IMPORT_ACTION_PROFILE("d14c3aa4-8d98-4c90-beac-71105a44711b", "actionProfiles"),
    IMPORT_MAPPING_PROFILE("d2dcffac-b5b1-40b7-aaf0-27d6a0987885", "mappingProfiles"),
    EXPORT_MAPPING_PROFILE("5d636597-a59d-4391-a270-4e79d5ba70e3", "mapping-profiles"),
    EXPORT_JOB_PROFILE("56944b1c-f3f9-475b-bed0-7387c33620ce", "job-profiles");

    private final String id;
    private final String url;
}
