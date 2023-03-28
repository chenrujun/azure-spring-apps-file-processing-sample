package com.azure.spring.example.file.processing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileMessageUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileMessageUtil.class);
    public static final String FILE_ORIGINAL_FILE = "file_originalFile";

    public static List<TxtLine> toTxtLine(Message<String> message) {
        String[] lines = message.getPayload().split("\\r?\\n");
        List<TxtLine> txtLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            txtLines.add(new TxtLine(i + 1, lines[i].trim()));
        }
        LOGGER.info("Split one file into lines. file = {}, lineNumber = {}.", getAbsolutePath(message), txtLines.size());
        return txtLines;
    }


    public static boolean isTargetFile(File file) {
        boolean isTargetFile = file.getName().endsWith(".txt");
        if (!isTargetFile) {
            LOGGER.info("File filtered out file because it's not txt file. File = {}.", file.getAbsolutePath());
        }
        return isTargetFile;
    }

    public static byte[] toAvroBytes(Message<TxtLine> message) {
        String absolutePath = getAbsolutePath(message);
        TxtLine line = message.getPayload();
        int lineNumber = line.lineNumber();
        String content = line.content();
        return AvroUtil.toAvroBytes(absolutePath, lineNumber, content);
    }

    public static String getAbsolutePath(Message<?> message) {
        return Optional.of(message)
                .map(Message::getHeaders)
                .map(headers -> (File) headers.get(FILE_ORIGINAL_FILE))
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
