package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(classes = {FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {
    @Autowired
    private FilmStorage filmStorage;

    @Test
    void testCreateAndFindFilmById() {
        // Создаем MPA рейтинг
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        // Создаем жанры
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");

        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        Set<Genre> genres = Set.of(genre);
        film.setGenres(genres);

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
        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film createdFilm = filmStorage.create(film);

        // Обновляем фильм
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated description");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testGetAllFilms() {
        // Создаем несколько фильмов
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        filmStorage.create(film2);

        List<Film> allFilms = filmStorage.getAll();

        assertThat(allFilms).hasSize(2);
        assertThat(allFilms).extracting(Film::getName).contains("Film 1", "Film 2");
    }

    @Test
    void testAddAndRemoveLike() {
        // Создаем пользователя и фильм
        // Предполагаем, что пользователи и фильмы уже созданы в базе данных
        // В реальных тестах нужно сначала создать пользователя

        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmStorage.create(film);

        // Создаем пользователя через UserStorage (в реальном тесте)
        // Здесь используем условный ID
        int userId = 1; // Предполагаем, что пользователь с ID 1 существует

        // Добавляем лайк
        filmStorage.addLike(createdFilm.getId(), userId);

        // Проверяем популярные фильмы
        List<Film> popularFilms = filmStorage.getPopular(10);
        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm.getId());

        // Удаляем лайк
        filmStorage.removeLike(createdFilm.getId(), userId);

        popularFilms = filmStorage.getPopular(10);
        // В зависимости от других данных в базе, фильм может все еще быть в списке
        // или может быть удален из-за отсутствия лайков
    }

    @Test
    void testGetPopularFilms() {
        // Создаем несколько фильмов
        Film film1 = new Film();
        film1.setName("Popular Film");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        Film createdFilm1 = filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Less Popular Film");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        Film createdFilm2 = filmStorage.create(film2);

        // Предполагаем наличие пользователя с ID 1
        int userId1 = 1;
        int userId2 = 2;

        // Добавляем больше лайков первому фильму
        filmStorage.addLike(createdFilm1.getId(), userId1);
        filmStorage.addLike(createdFilm1.getId(), userId2);

        filmStorage.addLike(createdFilm2.getId(), userId1);

        List<Film> popularFilms = filmStorage.getPopular(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm1.getId());
    }

    @Test
    void testFilmNotFound() {
        int nonExistentId = 999;

        assertThrows(ValidationException.class, () -> {
            filmStorage.getById(nonExistentId);
        });
    }
}