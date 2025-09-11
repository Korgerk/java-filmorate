package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("user_id");

        int key = simpleJdbcInsert.executeAndReturnKey(userToMap(user)).intValue();
        user.setId(key);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";
        int rows = jdbcTemplate.update(sql,

                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public User getById(Integer id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private java.util.Map<String, Object> userToMap(User user) {
        return java.util.Map.of("email", user.getEmail(), "login", user.getLogin(), "user_name", user.getName(), "birthday", user.getBirthday());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows == 0) {
            throw new RuntimeException("User with ID = " + id + " not found");
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sqlQuery = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, false)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " + "JOIN friends f ON u.user_id = f.friend_id " + "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(int user1Id, int user2Id) {
        String sql = "SELECT u.* FROM users u " + "JOIN friends f1 ON u.user_id = f1.friend_id " + "JOIN friends f2 ON u.user_id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), user1Id, user2Id);
    }

    @Override
    public boolean isFriend(int userId, int friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}