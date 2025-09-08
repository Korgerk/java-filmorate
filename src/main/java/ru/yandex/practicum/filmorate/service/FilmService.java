package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {

    private static final String FILM_NOT_FOUND_MSG = "Фильм с id=%d не найден.";

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (!filmStorage.exists(film.getId())) {
            throw new ValidationException(String.format(FILM_NOT_FOUND_MSG, film.getId()));
        }
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        if (!filmStorage.exists(id)) {
            throw new ValidationException(String.format(FILM_NOT_FOUND_MSG, id));
        }
        return filmStorage.getById(id);
    }

    public void addLike(int filmId, int userId) {
        if (!filmStorage.exists(filmId)) {
            throw new ValidationException(String.format(FILM_NOT_FOUND_MSG, filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (!filmStorage.exists(filmId)) {
            throw new ValidationException(String.format(FILM_NOT_FOUND_MSG, filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден.");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }
}