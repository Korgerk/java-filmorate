package ru.yandex.practicum.filmorate.storage.like;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;


import java.util.HashSet;
import java.util.List;


@Component
public class LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private MpaService mpaService;
    private GenreService genreService;

    @Autowired
    public LikeStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
                     "FROM films f " +
                     "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                     "GROUP BY f.id " +
                     "ORDER BY COUNT(fl.user_id) DESC " +
                     "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Film.class), count);
        for (Film film : films) {
            film.setLikes(new HashSet<>(getLikes(film.getId())));
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
            film.setGenres(genreService.getFilmGenres(film.getId()));
        }
        return films;
    }

    public List<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }
}