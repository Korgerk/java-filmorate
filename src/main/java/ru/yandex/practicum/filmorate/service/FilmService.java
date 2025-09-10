package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Transactional
public class FilmService {
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, 12, 28);
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getAll() {
        log.info("List of all movies: " + filmStorage.getAll().size());
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validate(film, "Movie form is filled in incorrectly");
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
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Movie with ID = " + id + " not found");
        }
        log.info("Requested movie with ID = " + id);
        return film;
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
        if (film == null) {
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }

        filmStorage.removeLike(filmId, userId);
        log.info("Like successfully removed");
    }

    public List<Film> getPopular(Integer count) {
        List<Film> result = filmStorage.getPopular(count);
        log.info("Requested a list of popular movies");
        return result;
    }

    private void validate(Film film, String message) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description length must be no more than 200 characters");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Release date cannot be null");
        }
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Release date cannot be earlier than 1895-12-28");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Release date cannot be in the future");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating cannot be null");
        }
    }
}