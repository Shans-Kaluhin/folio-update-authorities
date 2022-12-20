package org.folio;

import org.folio.service.UpdateAuthoritiesService;
import org.folio.util.FileWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class FolioUpdateAuthoritiesApp implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(FolioUpdateAuthoritiesApp.class);
    @Autowired
    private UpdateAuthoritiesService service;

    public static void main(String[] args) {
        SpringApplication.run(FolioUpdateAuthoritiesApp.class, args);
    }

    public static void exitWithError(String errorMessage) {
        LOG.error(errorMessage);
        System.exit(0);
    }

    public static void exitWithMessage(String message) {
        LOG.info(message);
        System.exit(0);
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            exitWithError("Please specify all parameters: configuration .json file path");
        }

        FileWorker.configurationFile = new File(args[0]);
        service.start();
    }
}
