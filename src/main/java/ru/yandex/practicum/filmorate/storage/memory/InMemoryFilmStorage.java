package ru.yandex.practicum.filmorate.storage.film.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>(); // filmId -> set of userIds
    private int generatedId = 1;

    @Override
    public Film create(Film film) {
        validateReleaseDate(film);
        film.setId(generatedId++);
        films.put(film.getId(), film);
        filmLikes.put(film.getId(), new HashSet<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден.");
        }
        validateReleaseDate(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getById(int id) {
        if (!films.containsKey(id)) {
            throw new ValidationException("Фильм с id=" + id + " не найден.");
        }
        return films.get(id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        if (!films.containsKey(filmId)) {
            throw new ValidationException("Фильм с id=" + filmId + " не найден.");
        }
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        if (!films.containsKey(filmId)) {
            throw new ValidationException("Фильм с id=" + filmId + " не найден.");
        }
        Set<Integer> likes = filmLikes.get(filmId);
        likes.remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return filmLikes.entrySet().stream().sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size())).limit(count).map(e -> films.get(e.getKey())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
    }
}