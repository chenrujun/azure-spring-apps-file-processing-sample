package com.azure.spring.example.file.processing.util;

import com.azure.spring.example.file.processing.avro.generated.User;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AvroUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroUtil.class);


    public static byte[] toAvroBytes(String file, int lineNumber, String string) {
        LOGGER.debug("Convert txt string to avro bytes. file = {}, lineNumber = {}, string = '{}'. ", file, lineNumber, string);
        User user = toUser(file, lineNumber, string);
        return userToAvroBytes(file, lineNumber, user);
    }

    public static User toUser(String file, int lineNumber, String string) {
        LOGGER.debug("Convert txt string to User. file = {}, lineNumber = {}, string = '{}'. ", file, lineNumber, string);
        if (string == null) {
            LOGGER.warn("Convert txt string to User failed: string = null. file = {}, lineNumber = {}.", file, lineNumber);
            return null;
        }
        String[] items = string.split(",");
        if (items.length != 3) {
            LOGGER.warn("Convert txt string to User failed: The string should has this format: '{name},{favorite_color},{favorite_number}'. file = {}, lineNumber = {}, string = '{}'. ", file, lineNumber, string);
            return null;
        }
        String name = items[0].trim();
        String color = items[1].trim();
        String number = items[2].trim();
        int favoriteNumber;
        try {
            favoriteNumber = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            LOGGER.warn("Convert txt string to User failed: favorite_number must be a number. But the actual favorite_number = {}. file = {}, lineNumber = {}, string = '{}'.", number, file, lineNumber, string);
            return null;
        }
        return User.newBuilder()
                .setName(name)
                .setFavoriteColor(color)
                .setFavoriteNumber(favoriteNumber)
                .build();
    }

    public static byte[] userToAvroBytes(String file, int lineNumber, User user) {
        LOGGER.debug("Convert txt User to avro bytes. file = {}, lineNumber = {}, user = '{}'. ", file, lineNumber, user);
        if (user == null) {
            return new byte[0];
        }
        File tempFile;
        try {
            tempFile = File.createTempFile("AvroUtil-", ".avro");
            writeUserToAvroFile(user, tempFile);
            return Files.readAllBytes(tempFile.toPath());
        } catch (IOException e) {
            LOGGER.warn("Convert txt User to avro bytes failed. file = {}, lineNumber = {}, user = '{}'.", file, lineNumber, user, e);
            return new byte[0];
        }
    }

    static User fromAvroBytes(byte[] bytes) throws IOException {
        LOGGER.debug("Read user from avro string. string = {}", bytes);
        File file = File.createTempFile("AvroUtil-", ".avro");
        Files.write(file.toPath(), bytes);
        return readUserFromAvroFile(file);
    }

    static void writeUserToAvroFile(User user, File file) throws IOException {
        LOGGER.debug("Write user to avro file. user = {}, file = {}.", user, file.getAbsolutePath());
        DatumWriter<User> userDatumWriter = new SpecificDatumWriter<>(User.class);
        DataFileWriter<User> dataFileWriter = new DataFileWriter<>(userDatumWriter);
        dataFileWriter.create(user.getSchema(), file);
        dataFileWriter.append(user);
        dataFileWriter.close();
    }

    static User readUserFromAvroFile(File file) throws IOException {
        LOGGER.debug("Read user from avro file. File = {}.", file.getAbsolutePath());
        DatumReader<User> userDatumReader = new SpecificDatumReader<>(User.class);
        try (DataFileReader<User> dataFileReader = new DataFileReader<>(file, userDatumReader)) {
            User user = dataFileReader.next();
            LOGGER.debug("Read user from avro file. user = {}, file = {}.", user, file.getAbsolutePath());
            return user;
        }
    }
}
