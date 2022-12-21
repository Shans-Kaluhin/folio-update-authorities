package org.folio.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataImportClient;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.folio.model.enums.JobStatus.CANCELLED;
import static org.folio.model.enums.JobStatus.COMMITTED;
import static org.folio.model.enums.JobStatus.DISCARDED;
import static org.folio.model.enums.JobStatus.ERROR;

@Slf4j
public class DataImportService {
    private static final String STATUS_BAR_TITLE = "IMPORT-PROGRESS-BAR  INFO --- [main] org.folio.service.DataImportService      : Update Authorities";
    private final DataImportClient dataImportClient;

    public DataImportService(DataImportClient dataImportClient) {
        this.dataImportClient = dataImportClient;
    }

    @SneakyThrows
    public void updateAuthority(Path authorityMrcFile, int recordsAmount) {
        var uploadDefinition = dataImportClient.uploadDefinition(authorityMrcFile);
        log.info("Update authority job id: " + uploadDefinition.getJobExecutionId());

        dataImportClient.uploadFile(uploadDefinition);
        dataImportClient.uploadJobProfile(uploadDefinition, "jobProfileInfo.json");

        waitForJobFinishing(buildProgressBar(recordsAmount), uploadDefinition.getJobExecutionId());
    }

    @SneakyThrows
    private void waitForJobFinishing(ProgressBar progressBar, String jobId) {
        var job = dataImportClient.retrieveJobExecution(jobId);
        progressBar.maxHint(job.getTotal());
        progressBar.stepTo(job.getCurrent());
        progressBar.setExtraMessage(job.getUiStatus());

        if (isJobFinished(job.getStatus())) {
            progressBar.close();
        } else {
            TimeUnit.SECONDS.sleep(20);
            waitForJobFinishing(progressBar, jobId);
        }
    }

    private ProgressBar buildProgressBar(int recordsAmount) {
        return new ProgressBarBuilder()
                .setInitialMax(recordsAmount)
                .setTaskName(STATUS_BAR_TITLE)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(STATUS_BAR_TITLE.length() + 70)
                .build();
    }

    private boolean isJobFinished(String status) {
        return COMMITTED.name().equals(status)
                || ERROR.name().equals(status)
                || CANCELLED.name().equals(status)
                || DISCARDED.name().equals(status);
    }
}
