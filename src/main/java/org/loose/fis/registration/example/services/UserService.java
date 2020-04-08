package org.loose.fis.registration.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.loose.fis.registration.example.exceptions.CouldNotWriteUsersException;
import org.loose.fis.registration.example.exceptions.UsernameAlreadyExists;
import org.loose.fis.registration.example.model.UserDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

public class UserService {

    private static List<UserDTO> users;

    public static void loadUsersFromFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        users = objectMapper.readValue(Paths.get("./config/users.json").toFile(), new TypeReference<List<UserDTO>>() {
        });
    }

    public static void addUser(String username, String password, String role) {
        checkUserDoesNotAlreadyExist(username);
        users.add(new UserDTO(username, encodePassword(username, password), role));
        persistUsers();
    }

    private static void checkUserDoesNotAlreadyExist(String username) {
        for (UserDTO user : users) {
            if (Objects.equals(username, user.getUsername()))
                throw new UsernameAlreadyExists(username);
        }
    }

    private static void persistUsers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get("./config/users.json").toFile(), users);
        } catch (IOException e) {
            throw new CouldNotWriteUsersException();
        }
    }

    private static String encodePassword(String salt, String password) {
        MessageDigest md = getMessageDigest();
        md.update(salt.getBytes(StandardCharsets.UTF_8));

        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // This is the way a password should be encoded when checking the credentials
        return new String(hashedPassword, StandardCharsets.UTF_8)
                .replace("\"", ""); //to be able to save in JSON format
    }

    private static MessageDigest getMessageDigest() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 does not exist!");
        }
        return md;
    }


}