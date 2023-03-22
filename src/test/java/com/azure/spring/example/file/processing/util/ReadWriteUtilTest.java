package com.azure.spring.example.file.processing.util;

import com.azure.spring.example.file.processing.avro.generated.User;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.azure.spring.example.file.processing.util.ReadWriteUtil.readUserFromAvroFile;
import static com.azure.spring.example.file.processing.util.ReadWriteUtil.readUsersFromTxtFile;
import static com.azure.spring.example.file.processing.util.ReadWriteUtil.toUser;
import static com.azure.spring.example.file.processing.util.ReadWriteUtil.writeUserToAvroFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReadWriteUtilTest {

    private static final String USER_1_STRING = "User1,blue,1";
    private static final User USER_1 = User.newBuilder()
            .setName("User1")
            .setFavoriteColor("blue")
            .setFavoriteNumber(1)
            .build();
    private static final User USER_2 = User.newBuilder()
            .setName("User2")
            .setFavoriteColor("red")
            .setFavoriteNumber(2)
            .build();

    @Test
    public void oneUserWriteThenReadTest() throws IOException {
        File file = File.createTempFile("ReadWriteUtilTest-", ".txt");
        writeUserToAvroFile(USER_1, file);
        User user = readUserFromAvroFile(file);
        assertEquals(USER_1, user);
    }

    @Test
    public void toUserTest() {
        assertEquals(USER_1, toUser(USER_1_STRING));
    }

    @Test
    public void readUsersFromTxtFileTest() throws IOException {
        URL url = getClass().getClassLoader().getResource("userString.txt");
        Objects.requireNonNull(url);
        File file = new File(url.getFile());
        List<User> users = readUsersFromTxtFile(file);
        assertEquals(4, users.size());
        assertEquals(USER_1, users.get(0));
        assertNull(users.get(1));
        assertEquals(USER_2, users.get(2));
        assertNull(users.get(3));
    }
}
