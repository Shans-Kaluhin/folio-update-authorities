package org.folio.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.folio.client.DataExportClient;
import org.folio.model.JobExecution;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.folio.model.enums.JobStatus.COMPLETED;
import static org.folio.model.enums.JobStatus.COMPLETED_WITH_ERRORS;
import static org.folio.model.enums.JobStatus.FAIL;

@Slf4j
public class DataExportService {
    private static final String STATUS_BAR_TITLE = "EXPORT-PROGRESS-BAR  INFO --- [main] : Export Authorities";
    private final DataExportClient dataExportClient;

    public DataExportService(DataExportClient dataExportClient) {
        this.dataExportClient = dataExportClient;
    }

    public String downloadFile(JobExecution jobExecution) {
        return dataExportClient.retrieveJobExecutionFile(jobExecution);
    }

    public JobExecution exportInventoryRecords(List<String> inventoryIds) {
        var jobId = dataExportClient.exportIds(inventoryIds);

        log.info("Export authority job id: {}", jobId);

        return waitForJobFinishing(buildProgressBar(inventoryIds.size()), jobId);
    }

    @SneakyThrows
    private JobExecution waitForJobFinishing(ProgressBar progressBar, String jobId) {
        var job = dataExportClient.retrieveJobExecution(jobId);
        progressBar.maxHint(job.getTotal());
        progressBar.stepTo(job.getCurrent());
        progressBar.setExtraMessage(job.getUiStatus());

        if (isJobFinished(job.getStatus())) {
            progressBar.close();
            return job;
        } else {
            TimeUnit.SECONDS.sleep(3);
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
