package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Transactional
public class FilmService {
    private static final LocalDate LIMIT_DATE = LocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0));
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private JdbcOperations jdbcTemplate;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getAll() {
        log.info("List of all movies: " + filmStorage.getAll().size());
        return filmStorage.getAll();
    }

    @Transactional
    public Film create(Film film) {
        validate(film, "Movie form is filled in incorrectly");

        if (film.getMpa() != null) {
            String checkMpaSql = "SELECT COUNT(*) FROM rating_mpa WHERE rating_id = ?";
            Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
            if (mpaCount == null || mpaCount == 0) {
                throw new NotFoundException("MPA rating with ID = " + film.getMpa().getId() + " not found");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                String checkGenreSql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
                Integer genreCount = jdbcTemplate.queryForObject(checkGenreSql, Integer.class, genre.getId());
                if (genreCount == null || genreCount == 0) {
                    throw new NotFoundException("Genre with ID = " + genre.getId() + " not found");
                }
            }
        }

        Film result = filmStorage.create(film);
        log.info("Movie successfully added: " + film);
        return result;
    }

    public Film update(Film film) {
        validate(film, "Movie update form is filled in incorrectly");

        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Movie with ID = " + film.getId() + " not found");
        }

        Film result = filmStorage.update(film);
        log.info("Movie successfully updated: {}", film);
        return result;
    }

    public Film getById(Integer id) {
        log.info("Requested user with ID = " + id);
        return filmStorage.getById(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId);
        User user = userStorage.getById(userId);

        if (film == null) {
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
        if (user == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Like successfully added to film {} by user {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId);
        if (film != null) {
            if (userStorage.getById(userId) != null) {
                filmStorage.removeLike(filmId, userId);
                log.info("Like successfully removed");
            } else {
                throw new NotFoundException("User with ID = " + userId + " not found");
            }
        } else {
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
    }

    public List<Film> getPopular(Integer count) {
        List<Film> result = new ArrayList<>(filmStorage.getPopular(count));
        log.info("Requested a list of popular movies");
        return result;
    }

    protected void validate(Film film, String message) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LIMIT_DATE)) {
            log.debug(message);
            throw new ValidationException(message);
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            log.debug("Release date cannot be in the future");
            throw new ValidationException("Release date cannot be in the future");
        }
        if (film.getDuration() <= 0) {
            log.debug("Duration must be positive");
            throw new ValidationException("Duration must be positive");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.debug("Description must be less than 200 characters");
            throw new ValidationException("Description must be less than 200 characters");
        }
    }
}