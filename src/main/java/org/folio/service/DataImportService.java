package org.folio.service;

import lombok.SneakyThrows;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataImportClient;
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
    public void updateAuthority(Path authorityMrcFile, int recordsAmount) {
        var uploadDefinition = dataImportClient.uploadDefinition(authorityMrcFile);
        LOG.info("Update authority job id: " + uploadDefinition.getJobExecutionId());

        dataImportClient.uploadFile(uploadDefinition);
        dataImportClient.uploadJobProfile(uploadDefinition, "jobProfileInfo.json");

        waitForJobFinishing(buildProgressBar(recordsAmount), uploadDefinition.getJobExecutionId());
    }

    @SneakyThrows
    private void waitForJobFinishing(ProgressBar progressBar, String jobId) {
        var job = dataImportClient.retrieveJobExecution(jobId);

        progressBar.setExtraMessage(job.getUiStatus());
        progressBar.maxHint(job.getTotal());
        progressBar.stepTo(job.getCurrent());

        if (isJobFinished(job.getStatus())) {
            progressBar.close();
        } else {
            TimeUnit.SECONDS.sleep(5);
            waitForJobFinishing(progressBar, jobId);
        }
    }

    private ProgressBar buildProgressBar(int recordsAmount) {
        return new ProgressBarBuilder()
                .setInitialMax(recordsAmount)
                .setTaskName("IMPORT-PROGRESS-BAR  INFO --- [main] org.folio.service.DataImportService      : Update Authorities")
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setMaxRenderedLength(150)
                .build();
    }

    private boolean isJobFinished(String status) {
        return COMMITTED.name().equals(status)
                || ERROR.name().equals(status)
                || CANCELLED.name().equals(status)
                || DISCARDED.name().equals(status);
    }
}
