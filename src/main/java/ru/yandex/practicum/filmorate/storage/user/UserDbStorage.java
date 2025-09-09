package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName()); // <-- Используем name
        parameters.put("birthday", user.getBirthday());

        Number newId = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(newId.intValue()); // Устанавливаем ID созданному пользователю
        log.info("User created: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rows = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new NotFoundException("User with ID = " + user.getId() + " not found");
        }
        log.info("User updated: {}", user);
        return user;
    }

    @Override
    public User getById(Integer id) { // <-- Исправлено с int на Integer
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with ID = " + id + " not found");
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {

        getById(userId);
        getById(friendId);

        String sql = "MERGE INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {

        getById(userId);
        getById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {

        getById(userId);

        String sql = "SELECT u.* FROM users u " + "JOIN friends f ON u.user_id = f.friend_id " + "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int user1Id, int user2Id) {

        getById(user1Id);
        getById(user2Id);

        String sql = "SELECT u.* FROM users u " + "JOIN friends f1 ON u.user_id = f1.friend_id " + "JOIN friends f2 ON u.user_id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, user1Id, user2Id);
    }

    public User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder().id(rs.getInt("user_id")).email(rs.getString("email")).login(rs.getString("login")).name(rs.getString("name")).birthday(rs.getDate("birthday").toLocalDate()).build();


        String friendsSql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> friendIds = jdbcTemplate.query(friendsSql, (rs1, rowNum1) -> rs1.getInt("friend_id"), user.getId());
        user.setFriendIds(friendIds.stream().collect(Collectors.toSet()));

        return user;
    }

    @Override
    public boolean isFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);

        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public void delete(int id) {
        getById(id);

        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(deleteUserSql, id);
    }
}
