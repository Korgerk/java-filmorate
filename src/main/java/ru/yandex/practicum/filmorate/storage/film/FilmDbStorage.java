package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_rating_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    };

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        saveFilmGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        deleteFilmGenres(film.getId());
        saveFilmGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        String sql = """
                SELECT f.*, mr.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
                WHERE f.id = ?
                """;
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                film.setGenres(getFilmGenres(id));
                film.setLikes(getFilmLikes(id));
            }
            return Optional.ofNullable(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = """
                SELECT f.*, mr.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(f -> {
            f.setGenres(getFilmGenres(f.getId()));
            f.setLikes(getFilmLikes(f.getId()));
        });
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*, mr.name AS mpa_name, COUNT(fl.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id, mr.name
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
            return film;
        }, count);
    }

    private Set<Genre> getFilmGenres(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g INNER JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId));
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        genres.forEach(g -> jdbcTemplate.update(sql, filmId, g.getId()));
    }

    private void deleteFilmGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Set<Long> getFilmLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }
}