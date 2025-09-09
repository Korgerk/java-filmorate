package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        userStorage.getUserById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        // Временная заглушка - нужно реализовать в хранилище
        return List.of();
    }

    public void addFriend(int userId, int friendId) {
        // Временная заглушка
    }

    public void removeFriend(int userId, int friendId) {
        // Временная заглушка
    }

    public List<User> getFriends(int userId) {
        // Временная заглушка
        return List.of();
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        // Временная заглушка
        return List.of();
    }
}