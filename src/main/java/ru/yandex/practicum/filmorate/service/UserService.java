package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public Set<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        if (!userStorage.exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!userStorage.exists(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (!userStorage.exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!userStorage.exists(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public Set<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }

    public Set<User> getCommonFriends(int userId, int otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }
}