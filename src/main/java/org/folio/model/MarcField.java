package org.folio.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class MarcField {
    private final String tag;
    private String value;
    private String ind1;
    private String ind2;
    private Map<Character, String> subfields;

    public MarcField(String tag, String ind1, String ind2, Map<Character, String> subfields) {
        this.tag = tag;
        this.ind1 = ind1;
        this.ind2 = ind2;
        this.subfields = subfields;
    }

    public MarcField(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }
}
