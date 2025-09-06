package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    Set<User> getAll();

    boolean exists(int id);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Set<User> getFriends(int userId);

    Set<User> getCommonFriends(int userId, int otherId);
}