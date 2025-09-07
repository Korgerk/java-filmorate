package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.impl.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {GenreDbStorage.class})
class GenreDbStorageTest {
    @Autowired
    private GenreStorage genreStorage;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAll();

        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getName).contains("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreStorage.getById(1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }
}