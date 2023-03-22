package com.azure.spring.example.file.processing.util;

import com.azure.spring.example.file.processing.avro.generated.User;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadWriteUtilTest {

    private static final User USER_1 = User.newBuilder()
            .setName("User1")
            .setFavoriteColor("blue")
            .setFavoriteNumber(3)
            .build();

    @Test
    public void oneUserWriteThenReadTest() throws IOException {
        File file = File.createTempFile("ReadWriteUtilTest-", ".txt");
        ReadWriteUtil.writeUserToAvroFile(USER_1, file);
        User user = ReadWriteUtil.readUserFromAvroFile(file);
        assertEquals(USER_1, user);
    }
}
