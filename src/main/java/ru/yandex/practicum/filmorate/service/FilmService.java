package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Slf4j
@Service
@Transactional
public class FilmService {
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, 12, 28);
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final MpaService mpaService;
    private final GenreService genreService;


    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, MpaService mpaService, GenreService genreService) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Collection<Film> getAll() {
        log.info("List of all movies: " + filmStorage.getAll().size());
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validate(film);
        Film result = filmStorage.create(film);
        log.info("Movie successfully added: " + film);
        return result;
    }

    public Film update(Film film) {
        validate(film);

        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            log.debug("Movie with ID = " + film.getId() + " not found");
            throw new NotFoundException("Movie with ID = " + film.getId() + " not found");
        }

        Film result = filmStorage.update(film);
        log.info("Movie successfully updated: {}", film);
        return result;
    }

    public Film getById(Integer id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            log.debug("Movie with ID = " + id + " not found");
            throw new NotFoundException("Movie with ID = " + id + " not found");
        }
        log.info("Requested movie with ID = " + id);
        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId);
        User user = userStorage.getById(userId);

        if (film == null) {
            log.debug("Movie with ID = " + filmId + " not found");
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
        if (user == null) {
            log.debug("User with ID = " + userId + " not found");
            throw new NotFoundException("User with ID = " + userId + " not found");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Like successfully added to film {} by user {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            log.debug("Movie with ID = " + filmId + " not found");
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
        if (userStorage.getById(userId) == null) {
            log.debug("User with ID = " + userId + " not found");
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

    private void validate(Film film) {

        if (film.getMpa() != null && film.getMpa().getId() > 0) {
            Mpa existingMpa = mpaService.getRatingMpaById(film.getMpa().getId());
            if (existingMpa == null) {
                log.debug("MPA rating with ID = {} not found", film.getMpa().getId());
                throw new ValidationException("MPA rating with ID = " + film.getMpa().getId() + " not found");
            }
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre == null) {
                    log.debug("Genre cannot be null");
                    throw new ValidationException("Genre cannot be null");
                }
                if (genre.getId() <= 0) {
                    log.debug("Invalid genre ID: " + genre.getId());
                    throw new ValidationException("Invalid genre ID");
                }

                Genre existingGenre = genreService.getGenreById(genre.getId());
                if (existingGenre == null) {
                    log.debug("Genre with ID = {} not found", genre.getId());
                    throw new ValidationException("Genre with ID = " + genre.getId() + " not found");
                }
            }
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.debug("Name cannot be empty");
            throw new ValidationException("Name cannot be empty");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.debug("Description length must be no more than 200 characters");
            throw new ValidationException("Description length must be no more than 200 characters");
        }

        if (film.getReleaseDate() == null) {
            log.debug("Release date cannot be null");
            throw new ValidationException("Release date cannot be null");
        }
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            log.debug("Release date cannot be earlier than 1895-12-28");
            throw new ValidationException("Release date cannot be earlier than 1895-12-28");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            log.debug("Release date cannot be in the future");
            throw new ValidationException("Release date cannot be in the future");
        }

        if (film.getDuration() <= 0) {
            log.debug("Duration must be positive");
            throw new ValidationException("Duration must be positive");
        }

        if (film.getMpa() == null) {
            log.debug("MPA rating cannot be null");
            throw new ValidationException("MPA rating cannot be null");
        }
        if (film.getMpa().getId() <= 0) {
            log.debug("Invalid MPA rating ID: " + film.getMpa().getId());
            throw new ValidationException("Invalid MPA rating ID");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre == null) {
                    log.debug("Genre cannot be null");
                    throw new ValidationException("Genre cannot be null");
                }
                if (genre.getId() <= 0) {
                    log.debug("Invalid genre ID: " + genre.getId());
                    throw new ValidationException("Invalid genre ID");
                }
            }
        }
    }
}