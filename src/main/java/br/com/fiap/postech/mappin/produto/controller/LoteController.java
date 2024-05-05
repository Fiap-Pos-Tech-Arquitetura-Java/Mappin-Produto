package br.com.fiap.postech.mappin.produto.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

@RestController
@RequestMapping("/lote")
public class LoteController {
    private final JobLauncher jobLauncher;
    private final Job job;
    private final JobExplorer jobExplorer;

    public LoteController(JobLauncher jobLauncher, @Qualifier("produtoExecucaoProgramadaBatchJob") Job job, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping("/execucaoManual")
    public ExitStatus execucaoManual(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = Files.createTempFile("produto", ".csv").toFile();
            file.transferTo(tempFile);

            JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                    .addJobParameter("novoArquivoProdutoCsv", tempFile.getAbsolutePath(), String.class)
                    .getNextJobParameters(job)
                    .toJobParameters();
            return this.jobLauncher.run(job, jobParameters).getExitStatus();
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/execucaoAgendada")
    public ExitStatus execucaoAgendada(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = Files.createTempFile("produto", ".csv").toFile();
            file.transferTo(tempFile);

            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(2);
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

            JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                    .addDate("launchDate", date)
                    .addJobParameter("novoArquivoProdutoCsv", tempFile.getAbsolutePath(), String.class)
                    .getNextJobParameters(job)
                    .toJobParameters();
            return this.jobLauncher.run(job, jobParameters).getExitStatus();
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
