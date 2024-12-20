package com.smartroute.book_network.file;

import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        try {
            Path path = Path.of(fileUrl);
            return Files.readAllBytes(path);

        } catch (IOException e) {
            log.warn("Failed to read file: {}", fileUrl, e);
        }

        return null;

    }

}
