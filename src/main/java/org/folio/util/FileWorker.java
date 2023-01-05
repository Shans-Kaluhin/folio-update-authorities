package org.folio.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.folio.model.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithError;

@Slf4j
public class FileWorker {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static File configurationFile;

    public static Configuration getConfiguration() {
        return getMappedFile(configurationFile, Configuration.class);
    }

    public static void saveConfiguration(Configuration configuration) {
        try (FileWriter fileWriter = new FileWriter(configurationFile)) {
            var objectWriter = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
            fileWriter.write(objectWriter.writeValueAsString(configuration));
        } catch (IOException e) {
            exitWithError("Failed to update configuration file");
        }
    }

    public static Path writeFile(String name, List<String> strings) {
        File file = new File(name);
        try (FileOutputStream writer = new FileOutputStream(name)) {
            for (var str : strings) {
                writer.write(str.getBytes(Charset.defaultCharset()));
            }
            return file.toPath();
        } catch (IOException e) {
            exitWithError("Failed to write file: " + name);
            return null;
        }
    }

    public static boolean deleteFile(Path path) {
        return path.toFile().delete();
    }

    public static InputStream getResourceFile(String name) {
        try {
            return ResourceUtils.getURL("classpath:" + name).openStream();
        } catch (IOException e) {
            exitWithError("Failed to read file: " + name);
            return null;
        }
    }

    public static <T> T getMappedFile(File file, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            exitWithError("Failed to map file value: " + file.getName());
            return null;
        }
    }

    public static ObjectNode getJsonObject(String name) {
        try {
            var file = getResourceFile(name);
            return (ObjectNode) OBJECT_MAPPER.readTree(file);
        } catch (IOException e) {
            exitWithError("Failed to map json file value: " + name);
            return null;
        }
    }
}
