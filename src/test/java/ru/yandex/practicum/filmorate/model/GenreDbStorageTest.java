package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.impl.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreStorage genreStorage;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAll();

        assertThat(genres).isNotNull();
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

    @Test
    void testGetGenreById2() {
        Genre genre = genreStorage.getById(2);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(2);
        assertThat(genre.getName()).isEqualTo("Драма");
    }

    @Test
    void testGetGenreById3() {
        Genre genre = genreStorage.getById(3);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(3);
        assertThat(genre.getName()).isEqualTo("Мультфильм");
    }

    @Test
    void testGetGenreById4() {
        Genre genre = genreStorage.getById(4);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(4);
        assertThat(genre.getName()).isEqualTo("Триллер");
    }

    @Test
    void testGetGenreById5() {
        Genre genre = genreStorage.getById(5);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(5);
        assertThat(genre.getName()).isEqualTo("Документальный");
    }

    @Test
    void testGetGenreById6() {
        Genre genre = genreStorage.getById(6);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(6);
        assertThat(genre.getName()).isEqualTo("Боевик");
    }

    @Test
    void testGetGenreByIdNotFound() {
        assertThrows(ValidationException.class, () -> genreStorage.getById(999));
    }

    @Test
    void testGenresOrder() {
        List<Genre> genres = genreStorage.getAll();

        // Проверяем, что жанры отсортированы по ID
        for (int i = 0; i < genres.size() - 1; i++) {
            assertThat(genres.get(i).getId()).isLessThan(genres.get(i + 1).getId());
        }
    }
}