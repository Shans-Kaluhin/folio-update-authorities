package org.folio.service;

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

@Service
public class UpdateAuthoritiesService {
    private SRSClient srsClient;
    private DataImportService importService;
    private MarcConverterService marcConverterService;
    private JobProfileService jobProfileService;

    private Configuration configuration;


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
        jobProfileService.populateProfiles();

        var totalRecords = srsClient.retrieveTotalRecords();
        while (configuration.getOffset() < totalRecords) {
            var records = srsClient.retrieveRecords(configuration.getLimit(), configuration.getOffset());
            incrementOffset(configuration, records.size());

            var mrcFile = marcConverterService.writeRecords(records);
            importService.updateAuthority(mrcFile);
            deleteFile(mrcFile);
        }

       // jobProfileService.deleteProfiles();
        return "Authorities was updated";
    }

    private void incrementOffset(Configuration configuration, int offset) {
        configuration.incrementOffset(offset);
        FileWorker.updateConfiguration(configuration);
    }
}
