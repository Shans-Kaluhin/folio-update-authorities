package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImportJobExecution {
    private String id;
    private String status;
    private String uiStatus;
    private int total;
    private int current;
}
