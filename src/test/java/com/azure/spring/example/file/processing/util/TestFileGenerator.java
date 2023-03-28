package com.azure.spring.example.file.processing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class TestFileGenerator {

    private static final Random random = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFileGenerator.class);

    public static void main(String[] args) {
        generateTestFiles("test-files/generated", true,"10_000-lines-", 10, 10_000);
    }

    // When lineCount = 1_00_000, the file size is 10 MB.
    public static void generateTestFiles(String directory, boolean deleteFolderFirst, String filePrefix, int fileCount, int lineCount) {
        File directoryFile = new File(directory);
        if (deleteFolderFirst) {
            if (!FileSystemUtils.deleteRecursively(directoryFile)) {
                LOGGER.info("Delete directory failed. directory = {}.", directory);
                return;
            }
        }
        if (!directoryFile.mkdirs()) {
            LOGGER.error("Create directory failed. directory = {}.", directory);
            return;
        }
        for (int i = 0; i < fileCount; i++) {
            File file = new File(directory, filePrefix + i + ".txt");
            try {
                if (!file.createNewFile()) {
                    LOGGER.error("Generate file failed. File = {}.", file.getAbsolutePath());
                }
                fulfillFile(file, lineCount);
                LOGGER.info("Generated file. File = {}, lineCount = {}, fileSize = {} (MB).", file.getAbsolutePath(), lineCount, file.length() / (1024 * 1024));
            } catch (IOException e) {
                LOGGER.error("Fulfill file error. ", e);
            }
        }
    }

    public static void fulfillFile(File file, int lineCount) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < lineCount; i++) {
            writer.write(generateLine());
            writer.newLine();
        }
        writer.close();
    }

    public static String generateLine() {
        return String.format("test_user__%s, test__color_%s, %s",
                UUID.randomUUID(),
                UUID.randomUUID(),
                random.nextInt(100_000));
    }
}
