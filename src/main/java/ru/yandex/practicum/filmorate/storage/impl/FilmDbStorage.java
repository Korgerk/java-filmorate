package ru.yandex.practicum.filmorate.storage.impl;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = new RowMapper<Film>() {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // Устанавливаем MPA рейтинг
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }
    };

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
    public Film create(Film film) {
        validateReleaseDate(film);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null);

        // Получаем сгенерированный ID
        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Integer.class);
        film.setId(id);

        // Сохраняем жанры, если они указаны
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenresForFilm(id, film.getGenres());
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        if (!exists(film.getId())) {
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден.");
        }

        validateReleaseDate(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        // Обновляем жанры
        if (film.getGenres() != null) {
            // Удаляем старые жанры
            jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
            // Добавляем новые жанры
            saveGenresForFilm(film.getId(), film.getGenres());
        }

        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, mr.id as mpa_rating_id, mr.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " + "ORDER BY f.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        // Загружаем жанры для каждого фильма
        for (Film film : films) {
            film.setGenres(getGenresForFilm(film.getId()));
        }

        return films;
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, mr.id as mpa_rating_id, mr.name as mpa_name " + "FROM films f " + "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " + "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film == null) {
                throw new ValidationException("Фильм с id=" + id + " не найден.");
            }
            film.setGenres(getGenresForFilm(id));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("Фильм с id=" + id + " не найден.");
        }
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(int filmId, int userId) {
        if (!exists(filmId)) {
            throw new ValidationException("Фильм с id=" + filmId + " не найден.");
        }
        if (!isUserExists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }

        // Проверяем, не поставил ли уже пользователь лайк этому фильму
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count == 0) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        if (!exists(filmId)) {
            throw new ValidationException("Фильм с id=" + filmId + " не найден.");
        }
        if (!isUserExists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, mr.id as mpa_rating_id, mr.name as mpa_name, COUNT(fl.user_id) as likes_count " + "FROM films f " + "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " + "LEFT JOIN film_likes fl ON f.id = fl.film_id " + "GROUP BY f.id, mr.id, mr.name " + "ORDER BY likes_count DESC " + "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // Устанавливаем MPA рейтинг
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }, count);

        // Загружаем жанры для каждого фильма
        for (Film film : films) {
            film.setGenres(getGenresForFilm(film.getId()));
        }

        return films;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
    }

    private boolean isUserExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    private void saveGenresForFilm(int filmId, Set<Genre> genres) {
        for (Genre genre : genres) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private Set<Genre> getGenresForFilm(int filmId) {
        String sql = "SELECT g.* FROM genres g " + "INNER JOIN film_genres fg ON g.id = fg.genre_id " + "WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, filmId);
        return new HashSet<>(genres);
    }
}