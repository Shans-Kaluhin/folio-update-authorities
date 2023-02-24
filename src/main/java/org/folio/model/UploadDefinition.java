package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadDefinition {
    private String uploadDefinitionId;
    private String jobExecutionId;
    private String fileId;
    private String fileBody;
}
