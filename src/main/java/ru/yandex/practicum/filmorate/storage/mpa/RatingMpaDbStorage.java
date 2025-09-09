package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RatingMpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public RatingMpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MpaRating getRatingMpaById(int ratingId) {
        String sqlQuery = "SELECT * FROM rating_mpa WHERE rating_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMpaRating, ratingId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<MpaRating> getRatingsMpa() {
        String sqlQuery = "SELECT * FROM rating_mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMpaRating);
    }

    private MpaRating mapRowToMpaRating(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("rating_id"), rs.getString("rating_name"));
    }
}
