package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getObject("birthday", LocalDate.class));
        return user;
    };

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            if (user != null) {
                user.setFriends(getUserFriendIds(id)); // ✅ Установи друзей
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        users.forEach(u -> u.setFriends(getUserFriendIds(u.getId())));
        return users;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO users_friends (user_id, friend_id, confirmed) VALUES (?, ?, FALSE)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        String sql = "UPDATE users_friends SET confirmed = TRUE WHERE user_id = ? AND friend_id = ?";
        int updated = jdbcTemplate.update(sql, userId, friendId);
        if (updated == 0) {
            throw new RuntimeException("Friend request not found");
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM users_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getUserFriends(Long userId) {
        String sql = """
                SELECT u.* FROM users u
                WHERE u.id IN (
                    SELECT friend_id FROM users_friends 
                    WHERE user_id = ? AND confirmed = TRUE
                )
                OR u.id IN (
                    SELECT user_id FROM users_friends 
                    WHERE friend_id = ? AND confirmed = TRUE
                )
                """;
        List<User> friends = jdbcTemplate.query(sql, userRowMapper, userId, userId);
        friends.forEach(f -> f.setFriends(getUserFriendIds(f.getId())));
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
                SELECT u.* FROM users u
                INNER JOIN users_friends uf1 ON u.id = uf1.friend_id
                INNER JOIN users_friends uf2 ON u.id = uf2.friend_id
                WHERE uf1.user_id = ? AND uf2.user_id = ?
                  AND uf1.confirmed = TRUE AND uf2.confirmed = TRUE
                """;
        List<User> common = jdbcTemplate.query(sql, userRowMapper, userId, otherId);
        common.forEach(u -> u.setFriends(getUserFriendIds(u.getId())));
        return common;
    }

    private Set<Long> getUserFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM users_friends WHERE user_id = ? AND confirmed = TRUE";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }
}