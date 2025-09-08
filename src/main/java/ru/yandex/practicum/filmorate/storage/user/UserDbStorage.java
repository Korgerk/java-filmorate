package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expectation.UserNotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Component("userDbStorage")
@Profile("jdbc")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue());
        log.info("Добавлен новый пользователь с ID={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (getUserById(user.getId()) != null) {
            String sqlQuery = "UPDATE users SET " + "email = ?, login = ?, name = ?, birthday = ? " + "WHERE id = ?";
            jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
            log.info("Пользователь с ID={} успешно обновлен", user.getId());
            return user;
        } else {
            throw new UserNotFoundException("Пользователь с ID=" + user.getId() + " не найден!");
        }
    }

    @Override
    public User getUserById(Long userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), userId);
    }

    @Override
    public User delete(Long userId) {
        if (userId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        User user = getUserById(userId);
        String sqlQuery = "DELETE FROM users WHERE id = ? ";
        if (jdbcTemplate.update(sqlQuery, userId) == 0) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return user;
    }
}