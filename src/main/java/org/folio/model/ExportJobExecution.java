package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportJobExecution {
    private String id;
    private String status;
    private String fileId;
    private int total;
    private int failed;
    private int exported;
}
