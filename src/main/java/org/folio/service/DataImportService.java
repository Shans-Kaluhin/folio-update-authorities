package org.folio.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataImportClient;

import java.util.concurrent.TimeUnit;

import static org.folio.model.enums.JobStatus.CANCELLED;
import static org.folio.model.enums.JobStatus.COMMITTED;
import static org.folio.model.enums.JobStatus.DISCARDED;
import static org.folio.model.enums.JobStatus.ERROR;
import static org.folio.model.enums.JobStatus.NOT_FOUND;

@Slf4j
public class DataImportService {
    private static final String STATUS_BAR_TITLE = "IMPORT-PROGRESS-BAR  INFO --- [main] : ";
    private static final int STATUS_CHECK_INTERVAL = 2;
    private final DataImportClient dataImportClient;

    public DataImportService(DataImportClient dataImportClient) {
        this.dataImportClient = dataImportClient;
    }

    public void updateAuthority(String fileBody, int recordsAmount) {
        var uploadDefinition = dataImportClient.uploadDefinition(fileBody);
        log.info("Update authority job id: {}", uploadDefinition.getJobExecutionId());

        dataImportClient.uploadFile(uploadDefinition);
        dataImportClient.uploadJobProfile(uploadDefinition, "jobProfileInfo.json");

        waitForJobFinishing(buildProgressBar("Update Authorities", recordsAmount), uploadDefinition.getJobExecutionId());
    }

    public void checkForExistedJob() {
        var existedJob = dataImportClient.retrieveFirstInProgressJob();
        if (!NOT_FOUND.name().equals(existedJob.getStatus())) {
            log.info("Discovered not finished data-import job: {}", existedJob.getId());

            waitForJobFinishing(buildProgressBar("Discovered job", existedJob.getTotal()), existedJob.getId());
            checkForExistedJob();
        }
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
            TimeUnit.SECONDS.sleep(STATUS_CHECK_INTERVAL);
            waitForJobFinishing(progressBar, jobId);
        }
    }

    private ProgressBar buildProgressBar(String title, int recordsAmount) {
        return new ProgressBarBuilder()
                .setInitialMax(recordsAmount)
                .setTaskName(STATUS_BAR_TITLE + title)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(STATUS_BAR_TITLE.length() + 80)
                .build();
    }

    private boolean isJobFinished(String status) {
        return COMMITTED.name().equals(status)
                || ERROR.name().equals(status)
                || CANCELLED.name().equals(status)
                || DISCARDED.name().equals(status);
    }
}
