package org.folio;

import lombok.extern.slf4j.Slf4j;
import org.folio.service.UpdateAuthoritiesService;
import org.folio.util.FileWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@Slf4j
@SpringBootApplication
public class FolioUpdateAuthoritiesApp implements CommandLineRunner {
    @Autowired
    private UpdateAuthoritiesService service;

    public static void main(String[] args) {
        SpringApplication.run(FolioUpdateAuthoritiesApp.class, args);
    }

    public static void exitWithError(String errorMessage) {
        log.error(errorMessage);
        System.exit(0);
    }

    public static void exitWithMessage(String message) {
        log.info(message);
        System.exit(0);
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            exitWithError("Please specify all parameters: configuration.json file path");
        }

        FileWorker.configurationFile = new File(args[0]);
        service.start();
    }
}
