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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ReadWriteUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadWriteUtil.class);


    public static byte[] txtStringToAvroBytes(String txtString) {
        LOGGER.debug("Convert txt string to avro bytes. txtString = '{}'. ", txtString);
        return toAvroBytes(txtStrngToUser(txtString));
    }

    public static void writeUserToAvroFile(User user, File file) throws IOException {
        LOGGER.debug("Write user to avro file. user = {}, file = {}.", user, file.getAbsolutePath());
        DatumWriter<User> userDatumWriter = new SpecificDatumWriter<>(User.class);
        DataFileWriter<User> dataFileWriter = new DataFileWriter<>(userDatumWriter);
        dataFileWriter.create(user.getSchema(), file);
        dataFileWriter.append(user);
        dataFileWriter.close();
    }

    public static User readUserFromAvroFile(File file) throws IOException {
        DatumReader<User> userDatumReader = new SpecificDatumReader<>(User.class);
        try (DataFileReader<User> dataFileReader = new DataFileReader<>(file, userDatumReader)) {
            User user = dataFileReader.next();
            LOGGER.debug("Read user from avro file. user = {}, file = {}.", user, file.getAbsolutePath());
            return user;
        }
    }

    public static List<User> readUsersFromTxtFile(File file) throws IOException {
        List<User> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                result.add(txtStrngToUser(line));
                line = reader.readLine();
            }
        }
        return result;
    }

    public static User txtStrngToUser(String string) {
        LOGGER.debug("Convert string to user. string = '{}'. ", string);
        if (string == null) {
            LOGGER.warn("Txt String is null.");
            return null;
        }
        String[] items = string.split(",");
        if (items.length != 3) {
            LOGGER.warn("Txt string is malformed. The right format is '{name},{favorite_color},{favorite_number}'. But the actual string = {}.", string);
            return null;
        }
        int favoriteNumber;
        try {
            favoriteNumber = Integer.parseInt(items[2]);
        } catch (NumberFormatException e) {
            LOGGER.warn("favorite_number is malformed. favorite_number must be a number. But the actual favorite_number = {}.", items[2]);
            return null;
        }
        return User.newBuilder()
                .setName(items[0])
                .setFavoriteColor(items[1])
                .setFavoriteNumber(favoriteNumber)
                .build();
    }

    /**
     * Convert {@link User} to avro bytes. If convert failed, return new byte[0].
     *
     * @param user The user to ve converted to avro bytes.
     * @return The avro bytes.
     */
    public static byte[] toAvroBytes(User user) {
        LOGGER.debug("Write user to avro string. user = {}", user);
        if (user == null) {
            return new byte[0];
        }
        File file;
        try {
            file = File.createTempFile("ReadWriteUtil-", ".avro");
            writeUserToAvroFile(user, file);
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error("User to avro bytes failed.", e);
            return new byte[0];
        }
    }

    public static User fromAvroBytes(byte[] bytes) throws IOException {
        LOGGER.debug("Read user from avro string. string = {}", bytes);
        File file = File.createTempFile("ReadWriteUtil-", ".avro");
        Files.write(file.toPath(), bytes);
        return readUserFromAvroFile(file);
    }
}
