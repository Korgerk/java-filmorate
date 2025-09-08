package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expectation.UserNotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component("inMemoryUserStorage")
@Profile("in-memory")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().trim().isEmpty() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }

        if (users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с таким ID уже существует");
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID=" + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new ValidationException("ID не может быть null");
        }
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден");
        }
        return user;
    }

    @Override
    public User delete(Long userId) {
        User user = getUserById(userId);
        users.remove(userId);
        return user;
    }
}