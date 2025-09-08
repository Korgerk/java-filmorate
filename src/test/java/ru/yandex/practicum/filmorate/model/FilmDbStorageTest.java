package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(mpa);
        testFilm.setGenres(Set.of(genre));
    }

    @Test
    void testCreateFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Test description");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
        assertThat(createdFilm.getGenres()).hasSize(1);
    }

    @Test
    void testGetFilmById() {
        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetFilmByIdNotFound() {
        assertThrows(ValidationException.class, () -> filmStorage.getById(999));
    }

    @Test
    void testUpdateFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated description");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testUpdateFilmNotFound() {
        testFilm.setId(999);
        assertThrows(ValidationException.class, () -> filmStorage.update(testFilm));
    }

    @Test
    void testGetAllFilms() {
        filmStorage.create(testFilm);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Another description");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(90);
        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        film2.setMpa(mpa);
        filmStorage.create(film2);

        List<Film> allFilms = filmStorage.getAll();
        assertThat(allFilms).hasSize(2);
        assertThat(allFilms).extracting(Film::getName).contains("Test Film", "Another Film");
    }

    @Test
    void testFilmExists() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(filmStorage.exists(createdFilm.getId())).isTrue();
        assertThat(filmStorage.exists(999)).isFalse();
    }

    @Test
    void testAddAndRemoveLike() {
        Film createdFilm = filmStorage.create(testFilm);
        int userId = 1;

        filmStorage.addLike(createdFilm.getId(), userId);

        // Проверяем, что фильм стал популярным
        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(createdFilm.getId());

        filmStorage.removeLike(createdFilm.getId(), userId);

        // После удаления лайка фильм не должен быть в топе
        popular = filmStorage.getPopular(10);
        assertThat(popular).isEmpty();
    }

    @Test
    void testAddLikeFilmNotFound() {
        assertThrows(ValidationException.class, () -> filmStorage.addLike(999, 1));
    }

    @Test
    void testAddLikeUserNotFound() {
        Film createdFilm = filmStorage.create(testFilm);
        assertThrows(ValidationException.class, () -> filmStorage.addLike(createdFilm.getId(), 999));
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = filmStorage.create(testFilm);

        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setDescription("Popular description");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film2.setMpa(mpa);
        Film createdFilm2 = filmStorage.create(film2);

        // Добавляем лайки
        filmStorage.addLike(createdFilm2.getId(), 1);
        filmStorage.addLike(createdFilm2.getId(), 8);
        filmStorage.addLike(film1.getId(), 1);

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        // Фильм с 2 лайками должен быть первым
        assertThat(popular.get(0).getId()).isEqualTo(createdFilm2.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film1.getId());
    }

    @Test
    void testGetPopularWithLimit() {
        for (int i = 0; i < 5; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setDescription("Description " + i);
            film.setReleaseDate(LocalDate.of(2020, 1, 1));
            film.setDuration(100 + i);
            MpaRating mpa = new MpaRating();
            mpa.setId(1);
            film.setMpa(mpa);
            Film created = filmStorage.create(film);

            if (i > 0) {
                filmStorage.addLike(created.getId(), 1);
            }
        }

        List<Film> popular = filmStorage.getPopular(3);
        assertThat(popular).hasSize(3);
    }

    @Test
    void testFilmWithGenres() {
        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getGenres()).isNotNull();
        assertThat(foundFilm.getGenres()).hasSize(1);
        assertThat(foundFilm.getGenres().iterator().next().getId()).isEqualTo(1);
        assertThat(foundFilm.getGenres().iterator().next().getName()).isEqualTo("Комедия");
    }

    @Test
    void testFilmWithMultipleGenres() {
        Genre genre1 = new Genre();
        genre1.setId(1);
        Genre genre2 = new Genre();
        genre2.setId(2);
        testFilm.setGenres(Set.of(genre1, genre2));

        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).contains(1, 2);
    }
}