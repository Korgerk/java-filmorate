package ru.yandex.practicum.filmorate.storage.film.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final RowMapper<Film> filmRowMapper = new FilmRowMapper();

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film create(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        if (!mpaStorage.exists(film.getMpa().getId())) {
            throw new ValidationException("MPA с id=" + film.getMpa().getId() + " не найден.");
        }
        for (Genre genre : film.getGenres()) {
            if (!genreStorage.exists(genre.getId())) {
                throw new ValidationException("Жанр с id=" + genre.getId() + " не найден.");
            }
        }
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("films").usingGeneratedKeyColumns("id");

        Map<String, Object> map = new HashMap<>();
        map.put("name", film.getName());
        map.put("description", film.getDescription());
        map.put("release_date", film.getReleaseDate());
        map.put("duration", film.getDuration());
        map.put("mpa_rating_id", film.getMpa().getId());

        Number key = insert.executeAndReturnKey(map);
        film.setId(key.intValue());

        saveFilmGenres(film.getId(), film.getGenres());

        updateGenres(film.getId(), film.getGenres());
        return film;
    }

    private void saveFilmGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());

        updateGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
        if (film != null) {
            film.setGenres(getGenresByFilmId(id));
        }
        return film;
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, COUNT(l.user_id) AS likes
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes DESC
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
        }
        return films;
    }

    private void updateGenres(int filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : genres) {
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        }
    }

    public Set<Genre> getGenresByFilmId(int filmId) {
        String sql = """
                SELECT g.id, g.name FROM genres g
                INNER JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                """;
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId));
    }

    private class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            int filmId = rs.getInt("id");
            Film film = Film.builder().id(filmId).name(rs.getString("name")).description(rs.getString("description")).releaseDate(rs.getDate("release_date").toLocalDate()).duration(rs.getInt("duration")).mpa(mpaStorage.getById(rs.getInt("mpa_rating_id"))).build();
            film.setGenres(getGenresByFilmId(filmId));
            return film;
        }
    }
}