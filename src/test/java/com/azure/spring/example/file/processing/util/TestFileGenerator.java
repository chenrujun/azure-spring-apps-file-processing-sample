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
        generateTestFiles("test-files/generated", 1, 100);
    }

    public static void generateTestFiles(String directory, int fileCount, int lineCount) {
        File directoryFile = new File(directory);
        if (!FileSystemUtils.deleteRecursively(directoryFile)) {
            LOGGER.info("Delete directory failed. directory = {}.", directory);
        }
        if (!directoryFile.mkdirs()) {
            LOGGER.error("Create directory failed. directory = {}.", directory);
        }
        for (int i = 0; i < fileCount; i++) {
            File file = new File(directory, "test-file-" + i + ".txt");
            try {
                if (!file.createNewFile()) {
                    LOGGER.error("Generate file failed. File = {}.", file.getAbsolutePath());
                }
                fulfillFile(file, lineCount);
                LOGGER.info("Generated file. File = {}.", file.getAbsolutePath());
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
        return String.format("User_%s,Color_%s,%s",
                UUID.randomUUID(),
                UUID.randomUUID(),
                random.nextInt(100));
    }
}
