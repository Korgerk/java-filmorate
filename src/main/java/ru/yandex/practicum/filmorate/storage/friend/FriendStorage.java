package ru.yandex.practicum.filmorate.storage.friend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Component
public class FriendStorage {
    private final JdbcTemplate jdbcTemplate;
    private UserStorage userStorage;

    @Autowired
    public FriendStorage(JdbcTemplate jdbcTemplate, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if ((user != null) && (friend != null)) {
            boolean status = false;
            if (friend.getFriends().contains(userId)) {
                status = true;  // дружба стала взаимной
                String sql = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
                jdbcTemplate.update(sql, true, friendId, userId);
            }
            String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, userId, friendId, status);
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if ((user != null) && (friend != null)) {
            String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(sql, userId, friendId);
            if (friend.getFriends().contains(userId)) {
                // дружба стала невзаимной - нужно поменять статус
                sql = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
                jdbcTemplate.update(sql, false, friendId, userId);
            }
        }
    }

    public List<User> getFriends(Long userId) {
        String sql = """
        SELECT u.id, u.email, u.login, u.name, u.birthday
        FROM friends f
        INNER JOIN users u ON f.friend_id = u.id
        WHERE f.user_id = ?
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), userId);
    }
}