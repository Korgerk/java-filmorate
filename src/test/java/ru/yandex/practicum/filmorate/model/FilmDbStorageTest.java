package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        // Создаём пользователя
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setLogin("testuser");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(testUser);

        // Создаём MPA и жанр
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");

        // Создаём фильм
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(mpa);
        testFilm.setGenres(Set.of(genre));

        filmStorage.create(testFilm);
    }

    @Test
    void testCreateAndFindFilmById() {
        Film foundFilm = filmStorage.getById(testFilm.getId());
        assertThat(foundFilm).usingRecursiveComparison().isEqualTo(testFilm);
        assertThat(foundFilm.getGenres()).hasSize(1);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).contains(1);
    }

    @Test
    void testUpdateFilm() {
        testFilm.setName("Updated Film");
        testFilm.setDescription("Updated description");

        Film updatedFilm = filmStorage.update(testFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testGetAllFilms() {
        List<Film> allFilms = filmStorage.getAll();
        assertThat(allFilms).hasSize(1);
        assertThat(allFilms.get(0).getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetPopularFilms() {
        // Создаём второй фильм
        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(120);
        film2.setMpa(new MpaRating() {{
            setId(1);
            setName("G");
        }});
        filmStorage.create(film2);

        // Добавляем больше лайков первому фильму
        filmStorage.addLike(testFilm.getId(), testUser.getId());

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user2);

        filmStorage.addLike(testFilm.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), testUser.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(testFilm.getId());
    }

    @Test
    void testFilmNotFound() {
        int nonExistentId = 999;
        assertThrows(ValidationException.class, () -> filmStorage.getById(nonExistentId));
    }
}