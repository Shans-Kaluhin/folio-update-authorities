package org.folio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {
    private String okapiUrl;
    private String tenant;
    private String username;
    private String password;
    private int limit;
    private int offset;

    public void incrementOffset(int increment) {
        offset += increment;
    }

    public void refreshOffset() {
        offset = 0;
    }
}
