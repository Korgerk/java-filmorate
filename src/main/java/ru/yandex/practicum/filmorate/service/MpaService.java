package ru.yandex.practicum.filmorate.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
public class MpaService {
    private final JdbcTemplate jdbcTemplate;

    public MpaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MpaRating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name")));
    }

    public MpaRating getById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name")), id);
    }
}