package org.folio.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobProfile {
    JOB_PROFILE("a2db814f-1106-4b08-90e0-39e2647e5438", "jobProfiles"),
    ACTION_PROFILE("d14c3aa4-8d98-4c90-beac-71105a44711b", "actionProfiles"),
    MAPPING_PROFILE("d2dcffac-b5b1-40b7-aaf0-27d6a0987885", "mappingProfiles"),
    MATCHING_PROFILE("ed99eff1-a008-47ff-b2e3-763315a01d9e", "matchingProfiles");

    private final String id;
    private final String url;
}
