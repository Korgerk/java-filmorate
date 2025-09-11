package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.like.LikesStorage;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage extends LikesStorage {

    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void delete(int id);

    Film getById(Integer id);

    void addGenre(int filmId, Set<Genre> genres);
}