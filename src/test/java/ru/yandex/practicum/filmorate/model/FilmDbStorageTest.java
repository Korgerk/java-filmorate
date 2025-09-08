package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.film.TestConfig;
import ru.yandex.practicum.filmorate.storage.film.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {FilmDbStorage.class, UserDbStorage.class, MpaDbStorage.class, GenreDbStorage.class, TestConfig.class})
@Import(TestConfig.class)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;
    private MpaRating mpa;
    private Set<Genre> genres;

    @BeforeEach
    void setUp() {
        // Создаём MPA и жанры (должны быть в data.sql)
        mpa = new MpaRating(1, "G");
        genres = Set.of(new Genre(1, "Комедия"));

        testFilm = Film.builder().name("Test Film").description("A test film").releaseDate(LocalDate.of(2020, 1, 1)).duration(120).mpa(mpa).genres(genres).build();
    }

    @Test
    void shouldCreateFilm() {
        Film created = filmStorage.create(testFilm);
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Test Film");
    }

    @Test
    void shouldGetFilmById() {
        Film created = filmStorage.create(testFilm);
        Film found = filmStorage.getById(created.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Film");
        assertThat(found.getGenres()).extracting("id").containsExactly(1);
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmStorage.create(testFilm);
        created.setName("Updated Name");
        filmStorage.update(created);

        Film updated = filmStorage.getById(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldGetAllFilms() {
        filmStorage.create(testFilm);
        List<Film> films = filmStorage.getAll();
        assertThat(films).hasSize(1);
        assertThat(films.get(0).getName()).isEqualTo("Test Film");
    }

    @Test
    void shouldAddLikeAndGetPopular() {
        // Создаём пользователя
        var user = userStorage.create(UserDbStorageTest.createTestUser("likeuser@example.com", "likeuser", "Like User", LocalDate.of(1990, 1, 1)));

        // Создаём фильм
        Film film = filmStorage.create(testFilm);

        // Добавляем лайк
        filmStorage.addLike(film.getId(), user.getId());

        // Проверяем, что фильм в топе
        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(film.getId());
    }

    @Test
    void shouldRemoveLike() {
        var user = userStorage.create(UserDbStorageTest.createTestUser("unlike@example.com", "unlike", "Un Like", LocalDate.of(1990, 1, 1)));
        Film film = filmStorage.create(testFilm);
        filmStorage.addLike(film.getId(), user.getId());

        // Проверяем, что лайк есть
        assertThat(getLikeCount(film.getId())).isEqualTo(1);

        // Удаляем
        filmStorage.removeLike(film.getId(), user.getId());

        // Проверяем, что лайк ушёл
        assertThat(getLikeCount(film.getId())).isEqualTo(0);
    }

    @Test
    void shouldGetPopularFilmsSortedByLikes() {
        var user1 = userStorage.create(UserDbStorageTest.createTestUser("u1@example.com", "u1", "U1", LocalDate.of(1990, 1, 1)));
        var user2 = userStorage.create(UserDbStorageTest.createTestUser("u2@example.com", "u2", "U2", LocalDate.of(1990, 1, 1)));

        Film film1 = filmStorage.create(Film.builder().name("Film 1").description("One like").releaseDate(LocalDate.now()).duration(90).mpa(mpa).build());

        Film film2 = filmStorage.create(Film.builder().name("Film 2").description("Two likes").releaseDate(LocalDate.now()).duration(90).mpa(mpa).build());

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId()); // Больше лайков
        assertThat(popular.get(1).getId()).isEqualTo(film1.getId());
    }

    private int getLikeCount(int filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId);
    }
}