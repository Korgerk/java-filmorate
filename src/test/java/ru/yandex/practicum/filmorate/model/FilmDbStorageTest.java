package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestPropertySource(properties = "spring.sql.init.mode=never")
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private FilmDbStorage filmDbStorage; // Только для внутренних проверок, если нужно

    private User createUser(String email, String login) {
        User user = User.builder()
                .email(email)
                .login(login)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        return userStorage.create(user);
    }

    @BeforeEach
    void setUp() {
        assertThat(mpaStorage.getById(1)).as("MPA должен существовать").isNotNull();
    }

    @Test
    void shouldCreateFilm() {
        // Создаем фильм
        MpaRating mpa = mpaStorage.getById(1);
        Film film = Film.builder()
                .name("Новый фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isPositive();
        assertThat(filmStorage.getById(created.getId()))
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(created);
    }

    @Test
    void shouldUpdateFilm() {
        // Создаем фильм
        MpaRating mpa = mpaStorage.getById(1);
        Film film = filmStorage.create(Film.builder()
                .name("Старое имя")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build());

        // Обновляем
        film.setName("Обновлённое имя");
        film.setDescription("Новое описание");
        film.setDuration(150);

        Film updated = filmStorage.update(film);

        assertThat(updated.getName()).isEqualTo("Обновлённое имя");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getDuration()).isEqualTo(150);
    }

    @Test
    void shouldAddLike() {
        User user1 = createUser("u1@example.com", "u1");
        User user2 = createUser("u2@example.com", "u2");

        MpaRating mpa = mpaStorage.getById(1);
        Film film = filmStorage.create(Film.builder()
                .name("Фильм с лайками")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build());

        filmStorage.addLike(film.getId(), user1.getId());
        filmStorage.addLike(film.getId(), user2.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular)
                .hasSize(1)
                .first()
                .extracting(Film::getId)
                .isEqualTo(film.getId());
    }

    @Test
    void shouldRemoveLike() {
        User user = createUser("u1@example.com", "u1");

        MpaRating mpa = mpaStorage.getById(1);
        Film film = filmStorage.create(Film.builder()
                .name("Фильм без лайков")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).doesNotContain(film);
    }

    @Test
    void shouldGetAllFilms() {
        MpaRating mpa = mpaStorage.getById(1);

        filmStorage.create(Film.builder()
                .name("Фильм 1")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build());

        filmStorage.create(Film.builder()
                .name("Фильм 2")
                .description("Описание 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build());

        List<Film> all = filmStorage.getAll();

        assertThat(all).hasSize(2);
    }
}