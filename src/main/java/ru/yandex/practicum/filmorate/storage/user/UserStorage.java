package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

import java.util.Collection;

public interface UserStorage extends FriendStorage {
    Collection<User> getAll();

    User create(User user);

    User update(User user);

    String delete(int id);

    User getById(Integer id);
}