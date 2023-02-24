package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {
    private String okapiUrl;
    private String tenant;
    private String username;
    private String password;
    private int importLimit;
    private int inventoryLimit;
    private int offset;

    public void incrementOffset(int increment) {
        offset += increment;
    }

    public void refreshOffset() {
        offset = 0;
    }
}
