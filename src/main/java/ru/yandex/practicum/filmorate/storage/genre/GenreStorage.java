package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.expectation.GenreNotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;


@Component
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Genre.class));
    }

    public Genre getGenreById(Integer genreId) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        Genre genre = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Genre.class), genreId);
        if (genre == null) {
            throw new GenreNotFoundException("Жанр с ID=" + genreId + " не найден!");
        }
        return genre;
    }

    public void delete(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
    }

    public void add(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    public List<Genre> getFilmGenres(Long filmId) {
        String sql = "SELECT genre_id AS id, name FROM film_genres " + "INNER JOIN genres ON genre_id = id WHERE film_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Genre.class), filmId);
    }
}