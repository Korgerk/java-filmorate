package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.awt.*;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    List<Film> getAll();

    Film getById(int id);

    void addLike(int filmId, int userId, RenderingHints users);

    void removeLike(int filmId, int userId, RenderingHints user);

    List<Film> getPopular(int count);
}