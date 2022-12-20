package org.folio.service;

import lombok.SneakyThrows;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataImportClient;
import org.folio.model.enums.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.folio.model.enums.JobStatus.CANCELLED;
import static org.folio.model.enums.JobStatus.COMMITTED;
import static org.folio.model.enums.JobStatus.DISCARDED;
import static org.folio.model.enums.JobStatus.ERROR;

public class DataImportService {
    private static final Logger LOG = LoggerFactory.getLogger(DataImportService.class);
    private final DataImportClient dataImportClient;

    public DataImportService(DataImportClient dataImportClient) {
        this.dataImportClient = dataImportClient;
    }

    @SneakyThrows
    public String updateAuthority(Path authorityMrcFile, int recordsAmount) {
        var uploadDefinition = dataImportClient.uploadDefinition(authorityMrcFile);
        LOG.info("Update authority job id: " + uploadDefinition.getJobExecutionId());

        dataImportClient.uploadFile(uploadDefinition);
        dataImportClient.uploadJobProfile(uploadDefinition, "jobProfileInfo.json");

        waitStatus(buildProgressBar(recordsAmount), uploadDefinition.getJobExecutionId(), COMMITTED);

        return uploadDefinition.getJobExecutionId();
    }

    @SneakyThrows
    private String waitStatus(ProgressBar progressBar, String jobId, JobStatus expectedStatus) {
        var status = dataImportClient.retrieveJobStatus(jobId);
        progressBar.step();
        if (status.equals(expectedStatus.name()) || status.equals(ERROR.name()) || status.equals(CANCELLED.name()) || status.equals(DISCARDED.name())) {
            LOG.info("Job finished with status: {}", status);
            progressBar.close();
            return status;
        } else {
            TimeUnit.SECONDS.sleep(20);
            return waitStatus(progressBar, jobId, expectedStatus);
        }
    }

    private ProgressBar buildProgressBar(int recordsAmount) {
        return new ProgressBarBuilder()
                .setInitialMax(recordsAmount)
                .setTaskName("Update authorities")
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .build();
    }
}
