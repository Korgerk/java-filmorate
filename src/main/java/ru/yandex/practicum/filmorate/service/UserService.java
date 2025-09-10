package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
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
        return result;
    }

    public User update(User user) {
        validate(user, "User update form is filled in incorrectly");
        preSave(user);
        if (userStorage.getById(user.getId()) == null) {
            throw new NotFoundException("User with ID = " + user.getId() + " not found");
        }
        User result = userStorage.update(user);
        return result;
    }

    public void delete(int userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }
        log.info("Deleted user with id: {}", userId);
        userStorage.delete(userId);
    }

    public User getById(Integer id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundException("User with ID = " + id + " not found");
        }
        log.info("Requested user with ID = " + id);
        return user;
    }

    public void addFriend(Integer userId, Integer friendId) {
        // Проверяем существование пользователей
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }
        if (userStorage.getById(friendId) == null) {
            throw new NotFoundException("User with ID = " + friendId + " not found");
        }

        // Запрещаем добавлять себя в друзья
        if (userId.equals(friendId)) {
            throw new ValidationException("User cannot add himself as a friend");
        }

        userStorage.addFriend(userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        // Проверяем существование пользователей
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }
        if (userStorage.getById(friendId) == null) {
            throw new NotFoundException("User with ID = " + friendId + " not found");
        }

        userStorage.removeFriend(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        // Проверяем существование пользователей
        if (userStorage.getById(user1Id) == null) {
            throw new NotFoundException("User with ID = " + user1Id + " not found");
        }
        if (userStorage.getById(user2Id) == null) {
            throw new NotFoundException("User with ID = " + user2Id + " not found");
        }

        return userStorage.getCommonFriends(user1Id, user2Id);
    }

    private void validate(User user, String message) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email must contain @ symbol");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Login cannot be empty");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Login cannot contain spaces");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Birthday cannot be null");
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