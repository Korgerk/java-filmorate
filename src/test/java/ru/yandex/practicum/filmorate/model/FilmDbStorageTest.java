package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.storage.film.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql("/schema.sql")
@Sql("/data.sql")
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Новый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(1);
        film.setGenres(List.of(1, 2));

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Новый фильм");
    }

    @Test
    void shouldAddLikeAndGetPopular() {
        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Film film = new Film();
        film.setName("Лучший фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(1);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(createdFilm.getId());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Старое имя");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(1);
        Film created = filmStorage.create(film);

        created.setName("Обновлённое имя");
        created.setDescription("Обновлённое описание");
        created.setDuration(150);

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Обновлённое имя");
        assertThat(updated.getDescription()).isEqualTo("Обновлённое описание");
        assertThat(updated.getDuration()).isEqualTo(150);
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film();
        film.setName("Фильм для поиска");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(1);
        Film created = filmStorage.create(film);

        Film found = filmStorage.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Фильм для поиска");
    }
}