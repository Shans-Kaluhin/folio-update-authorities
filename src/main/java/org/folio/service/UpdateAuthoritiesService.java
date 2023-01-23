package org.folio.service;

import lombok.extern.slf4j.Slf4j;
import org.folio.client.AuthClient;
import org.folio.client.DataImportClient;
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
    private JobProfileService jobProfileService;
    private MarcConverterService marcConverterService;

    public void start() {
        configuration = FileWorker.getConfiguration();

        var httpWorker = new HttpWorker(configuration);
        var authClient = new AuthClient(configuration, httpWorker);
        var importClient = new DataImportClient(httpWorker);
        var jobProfileClient = new JobProfilesClient(httpWorker);

        srsClient = new SRSClient(httpWorker);
        importService = new DataImportService(importClient);
        jobProfileService = new JobProfileService(jobProfileClient);
        marcConverterService = new MarcConverterService();

        httpWorker.setOkapiToken(authClient.authorize());

        var resultMessage = updateAuthorities();
        exitWithMessage(resultMessage);
    }

    private String updateAuthorities() {
        var totalRecords = srsClient.retrieveTotalRecords();
        validateTotalRecords(totalRecords);

        jobProfileService.populateProfiles();
        while (configuration.getOffset() < totalRecords) {
            var records = srsClient.retrieveRecordsPartitionaly(configuration.getLimit(), configuration.getOffset(), totalRecords);
            configuration.incrementOffset(records.size());
            saveConfiguration(configuration);

            var mrcFile = marcConverterService.writeRecords(records);
            importService.updateAuthority(mrcFile, records.size());
            deleteFile(mrcFile);
        }
        jobProfileService.deleteProfiles();

        return "Script execution completed";
    }

    private void validateTotalRecords(int totalRecords) {
        log.info("Total authority records: {}", totalRecords);

        if (totalRecords < 1) {
            exitWithMessage("There is no authorities to update");
        }

        if (configuration.getOffset() > 0) {
            log.warn("Found the offset value. Update will start from {}", configuration.getOffset());
        }

        if (configuration.getOffset() >= totalRecords) {
            log.warn("Offset is bigger then total records. Reset it to 0");
            configuration.refreshOffset();
            saveConfiguration(configuration);
        }
    }
}
