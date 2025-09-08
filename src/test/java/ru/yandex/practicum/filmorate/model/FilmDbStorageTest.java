package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldAddLike() {
        var user = userStorage.createUser(User.builder().email("user@film.ru").login("user1").birthday(LocalDate.now()).build());

        var film = filmStorage.createFilm(Film.builder().name("Likeable").releaseDate(LocalDate.now()).duration(100).mpa(new MpaRating(1, "G")).build());

        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> updated = filmStorage.findFilmById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLikes()).contains(user.getId());
    }

    @Test
    void shouldRemoveLike() {
        var user = userStorage.createUser(User.builder().email("user@film.ru").login("user1").birthday(LocalDate.now()).build());

        var film = filmStorage.createFilm(Film.builder().name("Likeable").releaseDate(LocalDate.now()).duration(100).mpa(new MpaRating(1, "G")).build());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Optional<Film> updated = filmStorage.findFilmById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLikes()).doesNotContain(user.getId());
    }

    @Test
    void shouldGetPopularFilms() {
        var user1 = userStorage.createUser(User.builder().email("u1@film.ru").login("u1").birthday(LocalDate.now()).build());
        var user2 = userStorage.createUser(User.builder().email("u2@film.ru").login("u2").birthday(LocalDate.now()).build());

        var film1 = filmStorage.createFilm(Film.builder().name("Popular 1").releaseDate(LocalDate.now()).duration(100).mpa(new MpaRating(1, "G")).build());

        var film2 = filmStorage.createFilm(Film.builder().name("Popular 2").releaseDate(LocalDate.now()).duration(100).mpa(new MpaRating(1, "G")).build());

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        var popular = filmStorage.getPopularFilms(10);
        assertThat(popular).hasSizeGreaterThanOrEqualTo(2);
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
    }
}