package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class ParsedRecord {
    private String id;
    private String leader;
    private List<MarcField> fields;
}
