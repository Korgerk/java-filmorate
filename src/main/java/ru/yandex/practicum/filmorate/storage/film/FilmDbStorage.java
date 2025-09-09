package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> getAll() {
        String sqlQuery = "SELECT f.*, rm.rating_name FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
        return addGenreForList(films);
    }

    @Override
    public Film create(Film film) {
        Map<String, Object> keys = new SimpleJdbcInsert(this.jdbcTemplate).withTableName("films").usingColumns("film_name", "description", "duration", "release_date", "rating_id").usingGeneratedKeyColumns("film_id").executeAndReturnKeyHolder(Map.of("film_name", film.getName(), "description", film.getDescription(), "duration", film.getDuration(), "release_date", java.sql.Date.valueOf(film.getReleaseDate()), "rating_id", film.getMpa().getId())).getKeys();
        film.setId((Integer) keys.get("film_id"));
        addGenre((Integer) keys.get("film_id"), film.getGenres());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE films " + "SET film_name = ?, description = ?, duration = ?, release_date = ?, rating_id = ? " + "WHERE film_id = ?";
        int rows = jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getDuration(), java.sql.Date.valueOf(film.getReleaseDate()), film.getMpa().getId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Movie with ID = " + film.getId() + " not found");
        }

        addGenre(film.getId(), film.getGenres());

        return getById(film.getId());
    }

    @Override
    public void delete(int filmId) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public Film getById(Integer filmId) {
        String sqlQuery = "SELECT f.*, rm.rating_name " + "FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id " + "WHERE f.film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, filmId);
            if (film != null) {
                // Загружаем жанры
                film.setGenres(new HashSet<>(getGenres(filmId)));
                return film;
            }
        } catch (EmptyResultDataAccessException e) {
            // ignored
        }
        throw new NotFoundException("Movie with ID = " + filmId + " not found");
    }

    public void addGenre(int filmId, Set<Genre> genres) {
        deleteAllGenresById(filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) " + "VALUES (?, ?)";
        List<Genre> genresTable = new ArrayList<>(genres);
        this.jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, genresTable.get(i).getId());
            }

            public int getBatchSize() {
                return genresTable.size();
            }
        });
    }

    private List<Genre> getGenres(int filmId) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name " + "FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.genre_id " + "WHERE fg.film_id = ? " + "ORDER BY g.genre_id";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("genre_name")), filmId);
    }

    private void deleteAllGenresById(int filmId) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    public void addLike(int filmId, int userId) {
        String sqlQuery = "INSERT INTO likes (film_id, user_id) " + "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sqlQuery = "DELETE FROM likes " + "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public List<Film> getPopular(Integer count) {
        String sqlQuery = "SELECT f.*, rm.rating_name, COUNT(l.user_id) as likes_count " + "FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id " + "LEFT JOIN likes l ON f.film_id = l.film_id " + "GROUP BY f.film_id " + "ORDER BY likes_count DESC " + "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm, count);
        return addGenreForList(films);
    }

    private List<Film> addGenreForList(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        Map<Integer, Film> filmsTable = films.stream().collect(Collectors.toMap(Film::getId, film -> film));

        String inSql = String.join(", ", Collections.nCopies(filmsTable.size(), "?"));
        final String sqlQuery = "SELECT fg.film_id, g.genre_id, g.genre_name " + "FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.genre_id " + "WHERE fg.film_id IN (" + inSql + ") " + "ORDER BY fg.film_id, g.genre_id";

        jdbcTemplate.query(sqlQuery, (rs) -> {
            Integer filmId = rs.getInt("film_id");
            Film film = filmsTable.get(filmId);
            if (film != null) { // ← проверка на null
                film.addGenre(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
            }
        }, filmsTable.keySet().toArray());

        return films;
    }

    private Genre makeGenre(ResultSet rs, int id) throws SQLException {
        int genreId = rs.getInt("genre_id");
        String genreName = rs.getString("genre_name");
        return new Genre(genreId, genreName);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpa = new MpaRating(rs.getInt("rating_id"), rs.getString("rating_name"));

        java.sql.Date sqlDate = rs.getDate("release_date");
        LocalDate releaseDate = sqlDate != null ? sqlDate.toLocalDate() : null;

        return Film.builder().id(rs.getInt("film_id")).name(rs.getString("film_name")).description(rs.getString("description")).duration(rs.getInt("duration")).releaseDate(releaseDate).mpa(mpa).genres(new HashSet<>()).build();
    }
}