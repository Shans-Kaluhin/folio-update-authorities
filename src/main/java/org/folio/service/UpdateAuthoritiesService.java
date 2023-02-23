package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import org.folio.client.AuthClient;
import org.folio.client.DataImportClient;
import org.folio.client.InventoryClient;
import org.folio.client.JobProfilesClient;
import org.folio.client.SRSClient;
import org.folio.model.Configuration;
import org.folio.util.FileWorker;
import org.folio.util.HttpWorker;
import org.springframework.stereotype.Service;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithMessage;
import static org.folio.util.FileWorker.deleteFile;
import static org.folio.util.FileWorker.saveConfiguration;

@Slf4j
@Service
public class UpdateAuthoritiesService {
    private SRSClient srsClient;
    private Configuration configuration;
    private DataImportService importService;
    private InventoryService inventoryService;
    private JobProfileService jobProfileService;
    private MarcConverterService marcConverterService;

    public void start() {
        configuration = FileWorker.getConfiguration();

        var httpWorker = new HttpWorker(configuration);
        var authClient = new AuthClient(configuration, httpWorker);
        var importClient = new DataImportClient(httpWorker);
        var inventoryClient = new InventoryClient(httpWorker);
        var jobProfileClient = new JobProfilesClient(httpWorker);

        srsClient = new SRSClient(httpWorker);
        importService = new DataImportService(importClient);
        jobProfileService = new JobProfileService(jobProfileClient);
        marcConverterService = new MarcConverterService();
        inventoryService = new InventoryService(inventoryClient);

        httpWorker.setOkapiToken(authClient.authorize());

        importService.checkForExistedJob();
        updateAuthorities();

        exitWithMessage("Script execution completed");
    }

    private void updateAuthorities() {
        var totalRecords = srsClient.retrieveTotalRecords();
        validateConfiguration(totalRecords);

        jobProfileService.populateProfiles();
        while (configuration.getOffset() < totalRecords) {
            var records = srsClient.retrieveRecordsPartitionaly(configuration, totalRecords);
            if (records.isEmpty()) {
                log.info("There is no srs records to update left");
                break;
            }

            // Inventory filter should be removed after fixing MODDATAIMP-780
            records = inventoryService.filterExistInventoryRecords(records);
            if (records.isEmpty()) {
                log.info("There is no linked inventory records to update left");
                continue;
            }

            configuration.incrementOffset(records.size());
            saveConfiguration(configuration);

            var mrcFile = marcConverterService.writeRecords(records);
            importService.updateAuthority(mrcFile, records.size());
            deleteFile(mrcFile);
        }
        jobProfileService.deleteProfiles();
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

        if (configuration.getSrsLimit() < 1) {
            log.warn("SRS limit is 0. Set it to 5000");
            configuration.setSrsLimit(5000);
        }

        if (configuration.getSrsLimit() > configuration.getImportLimit()) {
            log.warn("SRS limit is bigger then import. It will be reduced to import limit");
            configuration.setSrsLimit(configuration.getImportLimit());
        }

        if (configuration.getOffset() >= totalRecords) {
            log.warn("Offset is bigger then total records. Reset it to 0");
            configuration.refreshOffset();
        }
        saveConfiguration(configuration);
    }
}
