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
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    private MpaRating mpa;
    private Genre genre;

    @BeforeEach
    void setUp() {
        mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
    }

    @Test
    void testCreateAndFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(Set.of(genre));

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");

        Film foundFilm = filmStorage.getById(createdFilm.getId());
        assertThat(foundFilm).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(createdFilm);
        assertThat(foundFilm.getGenres()).hasSize(1);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).contains(1);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated description");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        film2.setMpa(mpa);
        filmStorage.create(film2);

        List<Film> allFilms = filmStorage.getAll();
        assertThat(allFilms).hasSize(2);
        assertThat(allFilms).extracting(Film::getName).contains("Film 1", "Film 2");
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        Film createdFilm = filmStorage.create(film);

        // Пользователь с id=1 уже создан в data.sql
        int userId = 1;

        filmStorage.addLike(createdFilm.getId(), userId);

        List<Film> popularFilms = filmStorage.getPopular(10);
        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm.getId());

        filmStorage.removeLike(createdFilm.getId(), userId);

        popularFilms = filmStorage.getPopular(10);
        assertThat(popularFilms).isEmpty();
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = new Film();
        film1.setName("Popular Film");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);
        Film createdFilm1 = filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Less Popular Film");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        film2.setMpa(mpa);
        Film createdFilm2 = filmStorage.create(film2);

        filmStorage.addLike(createdFilm1.getId(), 1);
        filmStorage.addLike(createdFilm1.getId(), 2);
        filmStorage.addLike(createdFilm2.getId(), 1);

        List<Film> popularFilms = filmStorage.getPopular(2);
        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm1.getId());
    }

    @Test
    void testFilmNotFound() {
        int nonExistentId = 999;
        assertThrows(ValidationException.class, () -> filmStorage.getById(nonExistentId));
    }
}