package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
        log.debug("Getting all films");
        try {
            String sqlQuery = "SELECT f.*, rm.rating_name FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id";
            List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
            log.debug("Found {} films", films.size());
            return addGenreForList(films);
        } catch (Exception e) {
            log.error("Error getting all films", e);
            throw new RuntimeException("Failed to get films", e);
        }
    }

    @Override
    public Film create(Film film) {
        log.debug("Creating film: {}", film);
        try {
            if (film.getMpa() != null) {
                String checkMpaSql = "SELECT COUNT(*) FROM rating_mpa WHERE rating_id = ?";
                Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
                if (mpaCount == null || mpaCount == 0) {
                    throw new NotFoundException("MPA rating with ID = " + film.getMpa().getId() + " not found");
                }
            }

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("films").usingGeneratedKeyColumns("film_id");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("film_name", film.getName());
            parameters.put("description", film.getDescription());
            parameters.put("duration", film.getDuration());
            parameters.put("release_date", java.sql.Date.valueOf(film.getReleaseDate()));
            parameters.put("rating_id", film.getMpa().getId());

            Number key = simpleJdbcInsert.executeAndReturnKey(parameters);
            film.setId(key.intValue());

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                addGenre(film.getId(), film.getGenres());
            }

            log.debug("Film created successfully with ID: {}", film.getId());
            return film;
        } catch (Exception e) {
            log.error("Error creating film: {}", film, e);
            throw new RuntimeException("Failed to create film", e);
        }
    }

    @Override
    public Film update(Film film) {
        log.debug("Updating film: {}", film);
        try {
            getById(film.getId());

            String sqlQuery = "UPDATE films " + "SET film_name = ?, description = ?, duration = ?, " + "release_date = ?, rating_id = ? " + "WHERE film_id = ?";

            jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getMpa().getId(), film.getId());

            addGenre(film.getId(), film.getGenres());
            Film updatedFilm = getById(film.getId());

            log.debug("Film updated successfully: {}", updatedFilm);
            return updatedFilm;
        } catch (Exception e) {
            log.error("Error updating film: {}", film, e);
            throw new RuntimeException("Failed to update film", e);
        }
    }

    @Override
    public void delete(int filmId) {
        log.debug("Deleting film with ID: {}", filmId);
        try {
            String sqlQuery = "DELETE FROM films WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery, filmId);
            log.debug("Film deleted successfully: {}", filmId);
        } catch (Exception e) {
            log.error("Error deleting film with ID: {}", filmId, e);
            throw new RuntimeException("Failed to delete film", e);
        }
    }

    @Override
    public Film getById(Integer filmId) {
        log.debug("Getting film by ID: {}", filmId);
        try {
            String sqlQuery = "SELECT f.*, rm.rating_name " + "FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id " + "WHERE f.film_id = ?";

            List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm, filmId);

            if (films.isEmpty()) {
                log.warn("Film with ID = {} not found", filmId);
                throw new NotFoundException("Movie with ID = " + filmId + " not found");
            }

            Film film = films.get(0);
            Set<Genre> genres = getGenres(filmId);
            film.setGenres(genres);

            log.debug("Found film: {}", film);
            return film;
        } catch (Exception e) {
            log.error("Error getting film by ID: {}", filmId, e);
            throw new RuntimeException("Failed to get film", e);
        }
    }

    public void addGenre(int filmId, Set<Genre> genres) {
        log.debug("Adding genres for film ID: {}, genres: {}", filmId, genres);
        try {
            deleteAllGenresById(filmId);
            if (genres == null || genres.isEmpty()) {
                return;
            }

            Set<Genre> uniqueGenres = genres.stream().filter(Objects::nonNull).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));

            String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Genre> genresList = new ArrayList<>(uniqueGenres);

            jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genresList.get(i).getId());
                }

                public int getBatchSize() {
                    return genresList.size();
                }
            });

            log.debug("Added {} genres for film ID: {}", genresList.size(), filmId);
        } catch (Exception e) {
            log.error("Error adding genres for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to add genres", e);
        }
    }

    private Set<Genre> getGenres(int filmId) {
        log.debug("Getting genres for film ID: {}", filmId);
        try {
            Set<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
            String sqlQuery = "SELECT film_genres.genre_id, genres.genre_name " + "FROM film_genres " + "JOIN genres ON genres.genre_id = film_genres.genre_id " + "WHERE film_id = ? ORDER BY genre_id ASC";

            genres.addAll(jdbcTemplate.query(sqlQuery, this::makeGenre, filmId));
            log.debug("Found {} genres for film ID: {}", genres.size(), filmId);
            return genres;
        } catch (Exception e) {
            log.error("Error getting genres for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to get genres", e);
        }
    }

    private void deleteAllGenresById(int filmId) {
        log.debug("Deleting all genres for film ID: {}", filmId);
        try {
            String sqlQuery = "DELETE FROM film_genres WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery, filmId);
            log.debug("Deleted all genres for film ID: {}", filmId);
        } catch (Exception e) {
            log.error("Error deleting genres for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to delete genres", e);
        }
    }

    public void addLike(int filmId, int userId) {
        log.debug("Adding like for film ID: {} by user ID: {}", filmId, userId);
        try {
            String sqlQuery = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlQuery, filmId, userId);
            log.debug("Like added successfully for film ID: {} by user ID: {}", filmId, userId);
        } catch (Exception e) {
            log.error("Error adding like for film ID: {} by user ID: {}", filmId, userId, e);
            throw new RuntimeException("Failed to add like", e);
        }
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Removing like for film ID: {} by user ID: {}", filmId, userId);
        try {
            String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
            jdbcTemplate.update(sqlQuery, filmId, userId);
            log.debug("Like removed successfully for film ID: {} by user ID: {}", filmId, userId);
        } catch (Exception e) {
            log.error("Error removing like for film ID: {} by user ID: {}", filmId, userId, e);
            throw new RuntimeException("Failed to remove like", e);
        }
    }

    @Override
    public List<Film> getPopular(Integer count) {
        log.debug("Getting popular films, count: {}", count);
        try {
            String sqlQuery = "SELECT f.*, rm.rating_name, COUNT(l.user_id) as likes_count " + "FROM films f " + "JOIN rating_mpa rm ON f.rating_id = rm.rating_id " + "LEFT JOIN likes l ON f.film_id = l.film_id " + "GROUP BY f.film_id " + "ORDER BY likes_count DESC " + "LIMIT ?";

            List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm, count);
            List<Film> result = addGenreForList(films);

            log.debug("Found {} popular films", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error getting popular films", e);
            throw new RuntimeException("Failed to get popular films", e);
        }
    }

    private List<Film> addGenreForList(List<Film> films) {
        log.debug("Adding genres for {} films", films.size());
        try {
            if (films.isEmpty()) {
                return films;
            }

            Map<Integer, Film> filmsTable = films.stream().collect(Collectors.toMap(Film::getId, film -> film));

            String inSql = String.join(",", Collections.nCopies(filmsTable.size(), "?"));
            String sqlQuery = "SELECT * " + "FROM film_genres " + "LEFT OUTER JOIN genres ON film_genres.genre_id = genres.genre_id " + "WHERE film_genres.film_id IN (" + inSql + ") " + "ORDER BY film_genres.genre_id";

            jdbcTemplate.query(sqlQuery, (rs) -> {
                Film film = filmsTable.get(rs.getInt("film_id"));
                if (film != null) {
                    film.addGenre(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
                }
            }, filmsTable.keySet().toArray());

            log.debug("Genres added for {} films", films.size());
            return films;
        } catch (Exception e) {
            log.error("Error adding genres for film list", e);
            throw new RuntimeException("Failed to add genres for film list", e);
        }
    }

    private Genre makeGenre(ResultSet rs, int id) throws SQLException {
        try {
            int genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");
            return new Genre(genreId, genreName);
        } catch (SQLException e) {
            log.error("Error creating Genre from ResultSet", e);
            throw e;
        }
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        try {
            MpaRating mpa = new MpaRating(rs.getInt("rating_id"), rs.getString("rating_name"));

            java.sql.Date releaseDateSql = rs.getDate("release_date");
            LocalDate releaseDate = releaseDateSql != null ? releaseDateSql.toLocalDate() : null;

            Film film = Film.builder().id(rs.getInt("film_id")).name(rs.getString("film_name")).description(rs.getString("description")).duration(rs.getInt("duration")).releaseDate(releaseDate).mpa(mpa).genres(new HashSet<>())
                    .build();

            return film;
        } catch (SQLException e) {
            log.error("Error creating Film from ResultSet", e);
            throw e;
        }
    }

    private Film filmMap(SqlRowSet srs) {
        try {
            int id = srs.getInt("film_id");
            String name = srs.getString("film_name");
            String description = srs.getString("description");
            int duration = srs.getInt("duration");

            LocalDate releaseDate = null;
            Object dateObj = srs.getObject("release_date");
            if (dateObj instanceof java.sql.Date) {
                releaseDate = ((java.sql.Date) dateObj).toLocalDate();
            } else if (dateObj instanceof java.sql.Timestamp) {
                releaseDate = ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
            }

            int mpaId = srs.getInt("rating_id");
            String mpaName = srs.getString("rating_name");

            MpaRating mpa = new MpaRating(mpaId, mpaName);
            Set<Genre> genres = getGenres(id);

            Film film = Film.builder().id(id).name(name).description(description).duration(duration).mpa(mpa).genres(genres).releaseDate(releaseDate).build();

            log.debug("Mapped film from SqlRowSet: {}", film);
            return film;
        } catch (Exception e) {
            log.error("Error mapping film from SqlRowSet", e);
            throw new RuntimeException("Failed to map film from database", e);
        }
    }
}