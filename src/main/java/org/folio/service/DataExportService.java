package org.folio.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataExportClient;
import org.folio.model.ExportJobExecution;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.folio.model.enums.JobStatus.COMPLETED;
import static org.folio.model.enums.JobStatus.COMPLETED_WITH_ERRORS;
import static org.folio.model.enums.JobStatus.FAIL;

@Slf4j
public class DataExportService {
    private static final String STATUS_BAR_TITLE = "EXPORT-PROGRESS-BAR  INFO --- [main] : Export Authorities";
    private static final int STATUS_CHECK_INTERVAL = 1;
    private final DataExportClient dataExportClient;

    public DataExportService(DataExportClient dataExportClient) {
        this.dataExportClient = dataExportClient;
    }

    public String downloadFile(ExportJobExecution jobExecution) {
        return dataExportClient.retrieveJobExecutionFile(jobExecution);
    }

    public ExportJobExecution exportInventoryRecords(List<String> inventoryIds) {
        var jobId = dataExportClient.exportIds(inventoryIds);

        log.info("Export authority job id: {}", jobId);

        var exportJob = waitForJobFinishing(buildProgressBar(inventoryIds.size()), jobId);
        var jobStatus = exportJob.getStatus();
        if (jobStatus.equals(COMPLETED_WITH_ERRORS.name())) {
            log.info("The export job finished with errors. Some records contain corrupted data");
        } else if (jobStatus.equals(FAIL.name()) && exportJob.getExported() > 0) {
            log.info("The export job failed, but the records were exported. See MDEXP-588");
            log.info("Trying to export the same UUIDs again");
            return exportInventoryRecords(inventoryIds);
        }
        return exportJob;
    }

    @SneakyThrows
    private ExportJobExecution waitForJobFinishing(ProgressBar progressBar, String jobId) {
        var job = dataExportClient.retrieveJobExecution(jobId);
        progressBar.maxHint(job.getTotal());
        progressBar.stepTo(job.getExported() + job.getFailed());
        progressBar.setExtraMessage(job.getStatus());

        if (isJobFinished(job.getStatus())) {
            progressBar.close();
            return job;
        } else {
            TimeUnit.SECONDS.sleep(STATUS_CHECK_INTERVAL);
            return waitForJobFinishing(progressBar, jobId);
        }
    }

    private ProgressBar buildProgressBar(int recordsAmount) {
        return new ProgressBarBuilder()
                .setInitialMax(recordsAmount)
                .setTaskName(STATUS_BAR_TITLE)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(STATUS_BAR_TITLE.length() + 80)
                .build();
    }

    private boolean isJobFinished(String status) {
        return COMPLETED.name().equals(status)
                || COMPLETED_WITH_ERRORS.name().equals(status)
                || FAIL.name().equals(status);
    }
}
