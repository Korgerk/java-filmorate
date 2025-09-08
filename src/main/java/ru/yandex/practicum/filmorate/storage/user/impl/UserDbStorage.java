package ru.yandex.practicum.filmorate.storage.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = new UserRowMapper();

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        // Если name не задан, устанавливаем login
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("id");

        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("login", user.getLogin());
        map.put("name", user.getName()); // уже установлено
        map.put("birthday", user.getBirthday());

        Number key = insert.executeAndReturnKey(map);
        user.setId(key.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
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
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, TRUE)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<User> getFriends(int userId) {
        String sql = """
                SELECT u.* FROM users u
                INNER JOIN friendships f ON u.id = f.friend_id
                WHERE f.user_id = ? AND f.confirmed = TRUE
                """;
        return new HashSet<>(jdbcTemplate.query(sql, userRowMapper, userId));
    }

    @Override
    public Set<User> getCommonFriends(int userId, int otherId) {
        String sql = """
                SELECT u.* FROM users u
                INNER JOIN friendships f1 ON u.id = f1.friend_id
                INNER JOIN friendships f2 ON u.id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                AND f1.confirmed = TRUE AND f2.confirmed = TRUE
                """;
        return new HashSet<>(jdbcTemplate.query(sql, userRowMapper, userId, otherId));
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return User.builder().id(rs.getInt("id")).email(rs.getString("email")).login(rs.getString("login")).name(rs.getString("name")).birthday(rs.getDate("birthday").toLocalDate()).build();
        }
    }
}