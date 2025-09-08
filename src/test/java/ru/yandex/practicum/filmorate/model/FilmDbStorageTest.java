package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.mpa.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private MpaStorage mpaStorage;

    @Autowired
    private GenreStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User createUser(String email, String login) {
        User user = User.builder().email(email).login(login).birthday(LocalDate.of(1990, 1, 1)).build();
        return userStorage.create(user);
    }

    @BeforeEach
    void setUp() {
        // Убедимся, что MPA и Genres загружены
        assertThat(mpaStorage.getById(1)).isNotNull();
        assertThat(genreStorage.getById(1)).isNotNull();
    }

    @Test
    void shouldCreateAndFindFilmById() {
        MpaRating mpa = mpaStorage.getById(1);
        Set<Genre> genres = Set.of(genreStorage.getById(1));

        Film film = Film.builder().name("Test Film").description("Description").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpa(mpa).genres(genres).build();

        Film created = filmStorage.create(film);
        Film found = filmStorage.getById(created.getId());

        assertThat(found).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(created);
        assertThat(found.getGenres()).hasSize(1).contains(genreStorage.getById(1));
    }

    @Test
    void shouldUpdateFilm() {
        MpaRating mpa = mpaStorage.getById(1);
        Film film = filmStorage.create(Film.builder().name("Before").description("Before").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).mpa(mpa).build());

        film.setName("After");
        film.setDescription("After");
        film.setDuration(150);

        Film updated = filmStorage.update(film);

        assertThat(updated.getName()).isEqualTo("After");
        assertThat(updated.getDescription()).isEqualTo("After");
        assertThat(updated.getDuration()).isEqualTo(150);
    }

    @Test
    void shouldAddLikeAndGetPopular() {
        User user1 = createUser("u1@example.com", "u1");
        User user2 = createUser("u2@example.com", "u2");

        Film film = filmStorage.create(Film.builder().name("Popular Film").description("Desc").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).mpa(mpaStorage.getById(1)).build());

        filmStorage.addLike(film.getId(), user1.getId());
        filmStorage.addLike(film.getId(), user2.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular).hasSize(1).first().satisfies(f -> assertThat(f.getId()).isEqualTo(film.getId()));
    }

    @Test
    void shouldRemoveLike() {
        User user = createUser("u1@example.com", "u1");
        Film film = filmStorage.create(Film.builder().name("Film").description("Desc").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).mpa(mpaStorage.getById(1)).build());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).doesNotContain(film);
    }

    @Test
    void shouldGetAllFilms() {
        filmStorage.create(Film.builder().name("Film 1").description("Desc 1").releaseDate(LocalDate.of(2000, 1, 1)).duration(100).mpa(mpaStorage.getById(1)).build());

        filmStorage.create(Film.builder().name("Film 2").description("Desc 2").releaseDate(LocalDate.of(2001, 1, 1)).duration(120).mpa(mpaStorage.getById(2)).build());

        List<Film> all = filmStorage.getAll();

        assertThat(all).hasSize(2);
    }
}