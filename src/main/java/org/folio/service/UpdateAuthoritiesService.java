package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import org.folio.client.AuthClient;
import org.folio.client.DataExportClient;
import org.folio.client.DataImportClient;
import org.folio.client.InventoryClient;
import org.folio.client.JobProfilesClient;
import org.folio.model.Configuration;
import org.folio.util.FileWorker;
import org.folio.util.HttpWorker;
import org.springframework.stereotype.Service;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithMessage;
import static org.folio.model.enums.JobStatus.FAIL;
import static org.folio.util.FileWorker.saveConfiguration;

@Slf4j
@Service
public class UpdateAuthoritiesService {
    private InventoryClient inventoryClient;
    private Configuration configuration;
    private DataImportService importService;
    private DataExportService dataExportService;
    private JobProfileService jobProfileService;

    public void start() {
        configuration = FileWorker.getConfiguration();

        var httpWorker = new HttpWorker(configuration);
        var authClient = new AuthClient(configuration, httpWorker);
        var importClient = new DataImportClient(httpWorker);
        var exportClient = new DataExportClient(httpWorker);
        var jobProfileClient = new JobProfilesClient(httpWorker);

        inventoryClient = new InventoryClient(httpWorker);
        importService = new DataImportService(importClient);
        jobProfileService = new JobProfileService(jobProfileClient);
        dataExportService = new DataExportService(exportClient);

        httpWorker.setOkapiToken(authClient.authorize());

        importService.checkForExistedJob();
        updateAuthorities();

        exitWithMessage("Script execution completed");
    }

    private void updateAuthorities() {
        var totalRecords = inventoryClient.retrieveTotalRecords();
        validateConfiguration(totalRecords);

        jobProfileService.populateExportProfiles();
        jobProfileService.populateImportProfiles();
        while (configuration.getOffset() < totalRecords) {
            var ids = inventoryClient.retrieveIdsPartitionaly(configuration, totalRecords);
            if (ids.isEmpty()) {
                log.info("There is no inventory records to update left");
                break;
            }

            configuration.incrementOffset(ids.size());
            saveConfiguration(configuration);

            var exportJob = dataExportService.exportInventoryRecords(ids);
            if (exportJob.getStatus().equals(FAIL.name())) {
                log.info("Export job failed. Continue with next records");
                continue;
            }
            var fileBody = dataExportService.downloadFile(exportJob);
            importService.updateAuthority(fileBody, ids.size());
        }
        jobProfileService.deleteImportProfiles();
    }

    private void validateConfiguration(int totalRecords) {
        log.info("Total authority records: {}", totalRecords);

        if (totalRecords < 1) {
            exitWithMessage("There is no authorities to update");
        }

        if (configuration.getOffset() > 0) {
            log.warn("Found the offset value. Update will start from {}", configuration.getOffset());
        }

        if (configuration.getImportLimit() < 1) {
            exitWithMessage("Import job limit is 0. Please specify the 'importLimit' field");
        }

        if (configuration.getInventoryLimit() < 1) {
            log.warn("SRS limit is 0. Set it to 5000");
            configuration.setInventoryLimit(5000);
        }

        if (configuration.getInventoryLimit() > configuration.getImportLimit()) {
            log.warn("SRS limit is bigger then import. It will be reduced to import limit");
            configuration.setInventoryLimit(configuration.getImportLimit());
        }

        if (configuration.getOffset() >= totalRecords) {
            log.warn("Offset is bigger then total records. Reset it to 0");
            configuration.refreshOffset();
        }
        saveConfiguration(configuration);
    }
}
