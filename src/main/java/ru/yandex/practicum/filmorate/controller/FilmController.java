package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final String LIKE_PATH = "/{id}/like/{userId}";

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        if (id <= 0) {
            throw new ValidationException("ID фильма должен быть положительным.");
        }
        return filmService.getById(id);
    }

    @PutMapping(LIKE_PATH)
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        if (id <= 0 || userId <= 0) {
            throw new ValidationException("ID должен быть положительным.");
        }
        filmService.addLike(id, userId);
    }

    @DeleteMapping(LIKE_PATH)
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        if (id <= 0 || userId <= 0) {
            throw new ValidationException("ID должен быть положительным.");
        }
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        if (count < 0) {
            throw new ValidationException("Count не может быть отрицательным.");
        }
        return filmService.getPopular(count);
    }
}