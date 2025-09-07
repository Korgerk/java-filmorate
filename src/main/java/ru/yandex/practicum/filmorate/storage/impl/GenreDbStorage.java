package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreRowMapper = new RowMapper<Genre>() {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }
    };

    @Override
    public Genre getById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("Жанр с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Genre> getAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }
}