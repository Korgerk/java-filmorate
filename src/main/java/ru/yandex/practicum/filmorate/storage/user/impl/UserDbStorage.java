package ru.yandex.practicum.filmorate.storage.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("id");

        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("login", user.getLogin());
        map.put("name", user.getName() != null ? user.getName() : user.getLogin());
        map.put("birthday", user.getBirthday());

        Number id = insert.executeAndReturnKey(map);
        user.setId(id.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    @Override
    public Set<User> getAll() {
        String sql = "SELECT * FROM users";
        return new HashSet<>(jdbcTemplate.query(sql, userRowMapper));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'confirmed')", userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public Set<User> getFriends(int userId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friendships f ON u.id = f.friend_id
                WHERE f.user_id = ? AND f.status = 'confirmed'
                """;
        return new HashSet<>(jdbcTemplate.query(sql, userRowMapper, userId));
    }

    @Override
    public Set<User> getCommonFriends(int userId, int otherId) {
        String sql = """
                SELECT u.* FROM users u
                WHERE u.id IN (
                    SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'confirmed'
                ) AND u.id IN (
                    SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'confirmed'
                )
                """;
        return new HashSet<>(jdbcTemplate.query(sql, userRowMapper, userId, otherId));
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}