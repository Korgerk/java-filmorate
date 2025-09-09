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
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class FilmService {
    private static final LocalDate LIMIT_DATE = LocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0));
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<Film> getAll() {
        log.info("List of all movies: {}", filmStorage.getAll().size());
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validate(film, "Movie form is filled in incorrectly");
        Film result = filmStorage.create(film);
        log.info("Movie successfully added: {}", film);
        return getById(result.getId());
    }

    public Film update(Film film) {
        validate(film, "Movie update form is filled in incorrectly");
        Film result = filmStorage.update(film);
        log.info("Movie successfully updated: {}", film);
        return result;
    }

    public Film getById(Integer id) {
        log.info("Requested film with ID = {}", id);
        return filmStorage.getById(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        filmStorage.addLike(filmId, userId);
        log.info("Like successfully added to film {} by user {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        filmStorage.removeLike(filmId, userId);
        log.info("Like successfully removed from film {} by user {}", filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        List<Film> result = filmStorage.getPopular(count == null ? 10 : count);
        log.info("Requested a list of popular movies (count: {})", count);
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

        MpaRating mpa = mpaStorage.getRatingMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new NotFoundException("MPA rating with ID = " + film.getMpa().getId() + " not found");
        }

        if (film.getGenres() != null) {
            Set<Integer> genreIds = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genre == null) {
                    throw new ValidationException("Genre cannot be null");
                }
                if (genre.getId() <= 0) {
                    throw new ValidationException("Genre ID must be positive");
                }
                if (genreIds.contains(genre.getId())) {
                    throw new ValidationException("Duplicate genres are not allowed");
                }
                genreIds.add(genre.getId());

                Genre existingGenre = genreStorage.getGenreById(genre.getId());
                if (existingGenre == null) {
                    throw new NotFoundException("Genre with ID = " + genre.getId() + " not found");
                }
            }
        }
    }
}