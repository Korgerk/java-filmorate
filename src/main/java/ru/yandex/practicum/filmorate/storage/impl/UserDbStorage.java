package ru.yandex.practicum.filmorate.storage.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }
    };

    @Override
    public User create(User user) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName() != null ? user.getName() : user.getLogin(), user.getBirthday());

        // Получаем сгенерированный ID
        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Integer.class);
        user.setId(id);

        // Создаем пустые списки друзей
        jdbcTemplate.update("INSERT INTO user_friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)", id, id, true);

        return user;
    }

    @Override
    public User update(User user) {
        if (!exists(user.getId())) {
            throw new ValidationException("Пользователь с id=" + user.getId() + " не найден.");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName() != null ? user.getName() : user.getLogin(), user.getBirthday(), user.getId());

        return user;
    }

    @Override
    public User getById(int id) {
        if (!exists(id)) {
            throw new ValidationException("Пользователь с id=" + id + " не найден.");
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("Пользователь с id=" + id + " не найден.");
        }
    }

    @Override
    public Set<User> getAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        return new LinkedHashSet<>(users);
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!exists(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }

        // Проверяем, не является ли пользователь уже другом
        String checkSql = "SELECT COUNT(*) FROM user_friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count == 0) {
            // Добавляем друга (односторонняя дружба)
            String sql = "INSERT INTO user_friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, userId, friendId, false);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (!exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!exists(friendId)) {
            throw new ValidationException("Пользователь с id=" + friendId + " не найден.");
        }

        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<User> getFriends(int userId) {
        if (!exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }

        String sql = "SELECT u.* FROM users u " + "INNER JOIN user_friends uf ON u.id = uf.friend_id " + "WHERE uf.user_id = ? AND uf.confirmed = TRUE";

        List<User> friends = jdbcTemplate.query(sql, userRowMapper, userId);
        return new HashSet<>(friends);
    }

    @Override
    public Set<User> getCommonFriends(int userId, int otherId) {
        if (!exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        if (!exists(otherId)) {
            throw new ValidationException("Пользователь с id=" + otherId + " не найден.");
        }

        String sql = "SELECT u.* FROM users u " + "INNER JOIN user_friends uf1 ON u.id = uf1.friend_id " + "INNER JOIN user_friends uf2 ON u.id = uf2.friend_id " + "WHERE uf1.user_id = ? AND uf2.user_id = ? " + "AND uf1.confirmed = TRUE AND uf2.confirmed = TRUE";

        List<User> commonFriends = jdbcTemplate.query(sql, userRowMapper, userId, otherId);
        return new HashSet<>(commonFriends);
    }
}