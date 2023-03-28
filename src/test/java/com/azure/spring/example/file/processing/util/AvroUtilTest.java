package com.azure.spring.example.file.processing.util;

import com.azure.spring.example.file.processing.avro.generated.User;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.azure.spring.example.file.processing.util.AvroUtil.fromAvroBytes;
import static com.azure.spring.example.file.processing.util.AvroUtil.readUserFromAvroFile;
import static com.azure.spring.example.file.processing.util.AvroUtil.toUser;
import static com.azure.spring.example.file.processing.util.AvroUtil.userToAvroBytes;
import static com.azure.spring.example.file.processing.util.AvroUtil.writeUserToAvroFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvroUtilTest {

    private static final String USER_1_TXT_STRING = "User1,blue,1";
    private static final User USER_1 = User.newBuilder()
            .setName("User1")
            .setFavoriteColor("blue")
            .setFavoriteNumber(1)
            .build();

    @Test
    public void userWriteToFileThenReadTest() throws IOException {
        File file = File.createTempFile("AvroUtilTest-", ".avro");
        writeUserToAvroFile(USER_1, file);
        User user = readUserFromAvroFile(file);
        assertEquals(USER_1, user);
    }

    @Test
    public void toUserTest() {
        assertEquals(USER_1, toUser("testFile", 0, USER_1_TXT_STRING));
    }

    @Test
    public void userToAvroBytesAndFromAvroBytesTest() throws IOException {
        assertEquals(USER_1, fromAvroBytes(userToAvroBytes("testFile", 0, USER_1)));
    }
}
