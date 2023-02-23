package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.InventoryClient;
import org.folio.model.ParsedRecord;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InventoryService {
    private static final String STATUS_BAR_TITLE = "FILTERING-INVENTORY  INFO --- [main] :";
    private final InventoryClient inventoryClient;

    public InventoryService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public List<ParsedRecord> filterExistInventoryRecords(List<ParsedRecord> srsRecords) {
        var filteredList = ProgressBar.wrap(srsRecords.stream(), buildProgressBar())
                .filter(r -> inventoryClient.isAuthorityExist(r.getExternalId()))
                .collect(Collectors.toList());

        log.info("Inventory records exist: {}", filteredList.size());

        return filteredList;
    }

    private ProgressBarBuilder buildProgressBar() {
        return new ProgressBarBuilder()
                .setTaskName(STATUS_BAR_TITLE)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(STATUS_BAR_TITLE.length() + 80);
    }
}
