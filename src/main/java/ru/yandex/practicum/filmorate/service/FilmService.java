package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final Mpa mpaStorage;

    public Film createFilm(Film film) {
        // Базовая валидация MPA
        if (film.getMpa() != null && film.getMpa().getId() > 0) {
            mpaStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new RuntimeException("MPA not found"));
        }
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        // Проверка существования фильма
        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new RuntimeException("Film not found"));

        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new RuntimeException("Film not found"));
    }

    public List<Film> getAllFilms() {
        // Временная заглушка - нужно реализовать в хранилище
        return List.of();
    }

    public void addLike(int filmId, int userId) {
        // Временная заглушка
    }

    public void removeLike(int filmId, int userId) {
        // Временная заглушка
    }

    public List<Film> getPopularFilms(int count) {
        // Временная заглушка
        return List.of();
    }
}