package ru.yandex.practicum.filmorate.storage.user.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> userFriends = new HashMap<>();
    private int generatedId = 1;

    @Override
    public User create(User user) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        user.setId(generatedId++);
        users.put(user.getId(), user);
        userFriends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с id=" + user.getId() + " не найден.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(int id) {
        if (!users.containsKey(id)) {
            throw new ValidationException("Пользователь с id=" + id + " не найден.");
        }
        return users.get(id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!users.containsKey(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }
        userFriends.get(userId).add(friendId);
        userFriends.get(friendId).add(userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!users.containsKey(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }
        userFriends.get(userId).remove(friendId);
        userFriends.get(friendId).remove(userId);
    }

    @Override
    public Set<User> getFriends(int userId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        return userFriends.get(userId).stream().map(users::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Set<User> getCommonFriends(int userId, int otherId) {
        if (!users.containsKey(userId) || !users.containsKey(otherId)) {
            throw new ValidationException("Пользователь не найден.");
        }
        Set<Integer> userFriendsSet = userFriends.get(userId);
        Set<Integer> otherFriendsSet = userFriends.get(otherId);
        return userFriendsSet.stream().filter(otherFriendsSet::contains).map(users::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}