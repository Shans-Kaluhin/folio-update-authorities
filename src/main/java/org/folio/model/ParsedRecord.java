package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class ParsedRecord {
    private String id;
    private String leader;
    private String externalId;
    private List<MarcField> fields;
}
