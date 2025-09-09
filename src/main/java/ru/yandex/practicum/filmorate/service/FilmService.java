package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class FilmService {
    private static final LocalDate LIMIT_DATE = LocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0));
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
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description cannot be longer than 200 characters");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Release date is too early");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Release date cannot be in the future");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating is required");
        }

        // Проверка MPA
        MpaRating mpa = mpaStorage.getRatingMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new NotFoundException("MPA rating not found");
        }

        // Проверка жанров на дубликаты
        if (film.getGenres() != null) {
            Set<Integer> genreIds = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genreIds.contains(genre.getId())) {
                    throw new ValidationException("Duplicate genres are not allowed");
                }
                genreIds.add(genre.getId());

                // Проверка существования жанра
                Genre existingGenre = genreStorage.getGenreById(genre.getId());
                if (existingGenre == null) {
                    throw new NotFoundException("Genre not found");
                }
            }
        }
    }
}