package org.folio.service;

import org.folio.client.AuthClient;
import org.folio.client.DataImportClient;
import org.folio.client.JobProfilesClient;
import org.folio.client.SRSClient;
import org.folio.model.Configuration;
import org.folio.util.HttpWorker;
import org.springframework.stereotype.Service;

import java.io.File;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithMessage;
import static org.folio.util.FileWorker.getMappedResourceFile;

@Service
public class UpdateAuthoritiesService {
    private SRSClient srsClient;
    private DataImportService importService;
    private MarcConverterService marcConverterService;
    private JobProfileService jobProfileService;

    public void start(File configurationFile) {
        var configuration = getMappedResourceFile(configurationFile, Configuration.class);
        var httpWorker = new HttpWorker(configuration);

        var authClient = new AuthClient(configuration, httpWorker);
        var importClient = new DataImportClient(httpWorker);
        var jobProfileClient = new JobProfilesClient(httpWorker);

        srsClient = new SRSClient(httpWorker, configuration);
        importService = new DataImportService(importClient);
        jobProfileService = new JobProfileService(jobProfileClient);
        marcConverterService = new MarcConverterService();

        httpWorker.setOkapiToken(authClient.authorize());

        var resultMessage = updateAuthorities();
        exitWithMessage(resultMessage);
    }

    private String updateAuthorities() {
        var records = srsClient.retrieveRecords();
        var mrcFile = marcConverterService.writeRecords(records);

        jobProfileService.populateProfiles();

        importService.updateAuthority(mrcFile);

        jobProfileService.deleteProfiles();

        return "Jobs for updating authorities started";
    }
}
