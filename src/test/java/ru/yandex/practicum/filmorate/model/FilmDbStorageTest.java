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
    void testCreateAndFindFilmById() {
        Film createdFilm = filmStorage.create(testFilm);
        assertThat(createdFilm.getId()).isNotNull();

        Film foundFilm = filmStorage.getById(createdFilm.getId());
        assertThat(foundFilm).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(createdFilm);
        assertThat(foundFilm.getGenres()).hasSize(1);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).contains(1);
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
    void testGetAllFilms() {
        filmStorage.create(testFilm);

        List<Film> allFilms = filmStorage.getAll();
        assertThat(allFilms).hasSize(1);
        assertThat(allFilms.get(0).getName()).isEqualTo("Test Film");
    }

    @Test
    void testAddAndRemoveLike() {
        Film createdFilm = filmStorage.create(testFilm);
        int userId = 1; // уже есть в data.sql

        filmStorage.addLike(createdFilm.getId(), userId);

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(createdFilm.getId());

        filmStorage.removeLike(createdFilm.getId(), userId);

        popular = filmStorage.getPopular(10);
        assertThat(popular).doesNotContain(createdFilm);
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = new Film();
        film1.setName("Popular Film");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new MpaRating() {{
            setId(1);
            setName("G");
        }});
        Film createdFilm1 = filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Less Popular");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        film2.setMpa(new MpaRating() {{
            setId(1);
            setName("G");
        }});
        Film createdFilm2 = filmStorage.create(film2);

        filmStorage.addLike(createdFilm1.getId(), 1);
        filmStorage.addLike(createdFilm1.getId(), 8);
        filmStorage.addLike(createdFilm2.getId(), 1);

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(createdFilm1.getId());
    }

    @Test
    void testFilmNotFound() {
        int nonExistentId = 999;
        assertThrows(ValidationException.class, () -> filmStorage.getById(nonExistentId));
    }
}