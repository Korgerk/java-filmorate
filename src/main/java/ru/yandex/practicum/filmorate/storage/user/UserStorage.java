package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

import java.util.List;

public interface UserStorage extends FriendStorage {
    List<User> getAll();

    User create(User user);

    User update(User user);

    void delete(int id);

    User getById(Integer id);
}