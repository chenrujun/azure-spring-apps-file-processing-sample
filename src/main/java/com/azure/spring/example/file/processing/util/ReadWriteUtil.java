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

public class ReadWriteUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadWriteUtil.class);

    public static void writeUserToAvroFile(User user, File file) throws IOException {
        LOGGER.debug("Write user to file. user = {}, file = {};", user, file.getAbsolutePath());
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
            LOGGER.debug("Read user from file. user = {}, file = {}. ", user, file.getAbsolutePath());
            return user;
        }
    }
}
