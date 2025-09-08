package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RatingMpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public RatingMpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MpaRating getRatingMpaById(int ratingId) {
        String sqlQuery = "SELECT * FROM rating_mpa WHERE rating_id = ?";
        SqlRowSet srs = jdbcTemplate.queryForRowSet(sqlQuery, ratingId);
        if (srs.next()) {
            return new MpaRating(ratingId, srs.getString("rating_name"));
        }
        return null;
    }

    public List<MpaRating> getRatingsMpa() {
        List<MpaRating> ratingsMpa = new ArrayList<>();
        String sqlQuery = "SELECT * FROM rating_mpa";
        SqlRowSet srs = jdbcTemplate.queryForRowSet(sqlQuery);
        while (srs.next()) {
            ratingsMpa.add(new MpaRating(srs.getInt("rating_id"), srs.getString("rating_name")));
        }
        return ratingsMpa;
    }
}