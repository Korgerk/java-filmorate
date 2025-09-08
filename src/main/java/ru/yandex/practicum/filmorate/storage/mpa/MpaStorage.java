package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expectation.MpaNotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.MpaRating;


import java.util.List;

@Component
public class MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MpaRating> getAllMpa() {
        String sql = "SELECT * FROM ratings_mpa";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MpaRating.class));
    }

    public MpaRating getMpaById(Integer mpaId) {
        String sql = "SELECT * FROM ratings_mpa WHERE id = ?";
        MpaRating mpa = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(MpaRating.class), mpaId);
        if (mpa == null) {
            throw new MpaNotFoundException("Рейтинг с ID=" + mpaId + " не найден!");
        }
        return mpa;
    }
}