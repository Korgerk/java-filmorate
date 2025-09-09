package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        validate(user, "User form is filled in incorrectly");
        preSave(user);
        User result = userStorage.create(user);
        log.info("User successfully added: {}", user);
        return result;
    }

    public User update(User user) {
        validate(user, "User update form is filled in incorrectly");
        preSave(user);
        User result = userStorage.update(user);
        log.info("User successfully updated: {}", user);
        return result;
    }

    public User getById(Integer id) {
        log.info("Requested user with ID = {}", id);
        return userStorage.getById(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        userStorage.addFriend(userId, friendId);
        log.info("User {} and user {} became friends", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("User {} and user {} are no longer friends", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        List<User> result = userStorage.getFriends(userId);
        log.info("Requested friends list for user ID {}", userId);
        return result;
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        List<User> result = userStorage.getCommonFriends(user1Id, user2Id);
        log.info("Common friends of users with ID {} and {} requested", user1Id, user2Id);
        return result;
    }

    private void validate(User user, String message) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.debug(message);
            throw new ValidationException("Email cannot be empty");
        }
        if (!user.getEmail().contains("@")) {
            log.debug(message);
            throw new ValidationException("Email should be valid");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.debug(message);
            throw new ValidationException("Login cannot be empty");
        }
        if (user.getLogin().contains(" ")) {
            log.debug(message);
            throw new ValidationException("Login cannot contain spaces");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            log.debug(message);
            throw new ValidationException("Birthday is required");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug(message);
            throw new ValidationException("Birthday cannot be in the future");
        }
    }

    private void preSave(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
