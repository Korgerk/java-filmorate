package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.impl.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {GenreDbStorage.class})
class GenreDbStorageTest {

    @Autowired
    private GenreStorage genreStorage;

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreStorage.getAll();
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1);
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = genreStorage.getById(2);
        assertThat(genre).hasFieldOrPropertyWithValue("id", 2);
        assertThat(genre).hasFieldOrPropertyWithValue("name", "Драма");
    }
}