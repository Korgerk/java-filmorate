package ru.yandex.practicum.filmorate.storage.user;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    default Optional<User> findUserById(Long id) {
        return null;
    }

    List<User> getAllUsers();

    void addFriend(Long userId, Long friendId);

    void confirmFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getUserFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);
}