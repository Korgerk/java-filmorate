package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expectation.FilmNotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component("inMemoryFilmStorage")
@Profile("in-memory")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28.12.1895");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("Рейтинг MPA обязателен");
        }

        if (film.getId() == null) {
            film.setId(idGenerator.getAndIncrement());
        }

        if (films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с таким ID уже существует");
        }

        film.setLikes(new HashSet<>());
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }
        if (!films.containsKey(film.getId())) {
            throw new FilmNotFoundException("Фильм с ID=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("ID не может быть null");
        }
        Film film = films.get(filmId);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден");
        }
        return film;
    }

    @Override
    public Film delete(Long filmId) {
        Film film = getFilmById(filmId);
        films.remove(filmId);
        return film;
    }
}