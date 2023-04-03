package com.azure.spring.example.file.processing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.regex.Matcher.quoteReplacement;

public class FileMessageUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileMessageUtil.class);
    public static final String FILE_ORIGINAL_FILE = "file_originalFile";
    public static final String LINE_NUMBER_IN_FILE = "lineNumberInFile";

    public static List<TxtLine> toTxtLineThenMoveFile(Message<String> message, String logsDirectory, String processedLogsDirectory) {
        String[] lines = message.getPayload().split("\\r?\\n");
        List<TxtLine> txtLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            txtLines.add(new TxtLine(i + 1, lines[i].trim()));
        }
        LOGGER.info("Split one file into lines. file = {}, lineNumber = {}.", getAbsolutePath(message), txtLines.size());
        moveFile(message, logsDirectory, processedLogsDirectory);
        return txtLines;
    }

    public static void moveFile(Message<String> message, String logsDirectory, String processedLogsDirectory) {
        File file = getOptionalFile(message).orElse(null);
        if (file == null) {
            LOGGER.warn("Move file failed because file is null.");
            return;
        }
        String oldPath = getAbsolutePath(message);
        String newPath = newPath(oldPath, logsDirectory, processedLogsDirectory);
        File newFile = new File(newPath);
        try {
            Files.createDirectories(newFile.getParentFile().getAbsoluteFile().toPath());
        } catch (IOException e) {
            LOGGER.warn("Move file failed because cannot create directory. oldPath = {}, newPath = {}.", oldPath, newPath, e);
            return;
        }
        boolean moveSucceed = file.renameTo(newFile);
        if (moveSucceed) {
            LOGGER.info("Move file succeed. oldPath = {}, newPath = {}.", oldPath, newPath);
        } else {
            LOGGER.warn("Move file failed. oldPath = {}, newPath = {}.", oldPath, newPath);
        }
    }

    private static String newPath(String currentPath, String logsDirectory, String processedLogsDirectory) {
        return currentPath.replaceFirst(quoteReplacement(logsDirectory), quoteReplacement(processedLogsDirectory));
    }


    public static boolean isTargetFile(File file) {
        boolean isTargetFile = file.getName().endsWith(".txt");
        if (!isTargetFile) {
            LOGGER.info("File filtered out file because it's not txt file. File = {}.", file.getAbsolutePath());
        }
        return isTargetFile;
    }

    /**
     * Convert {@link TxtLine} to byte array.
     * @param message The message which holds TxtLine as payload.
     * @return The byte array. Empty array if convert failed.
     */
    public static byte[] toAvroBytes(Message<TxtLine> message) {
        String absolutePath = getAbsolutePath(message);
        TxtLine line = message.getPayload();
        int lineNumber = line.lineNumber();
        String content = line.content();
        return AvroUtil.toAvroBytes(absolutePath, lineNumber, content);
    }

    public static Optional<File> getOptionalFile(Message<?> message) {
        return Optional.of(message)
                .map(Message::getHeaders)
                .map(headers -> (File) headers.get(FILE_ORIGINAL_FILE));
    }

    public static String getAbsolutePath(Message<?> message) {
        return getOptionalFile(message)
                .map(File::getAbsolutePath)
                .orElse(null);
    }

    public static String getFileSize(Message<?> message) {
        return Optional.of(message)
                .map(Message::getHeaders)
                .map(headers -> (File) headers.get(FILE_ORIGINAL_FILE))
                .map(File::length)
                .map(FileMessageUtil::readableFileSize)
                .orElse(null);
    }

    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
