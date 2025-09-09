package ru.yandex.practicum.filmorate.model;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    private int createUser(String email, String login, String name, LocalDate birthday) {
        var insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("login", login);
        parameters.put("name", name);
        parameters.put("birthday", birthday);

        return insert.executeAndReturnKey(parameters).intValue();
    }

    @Test
    public void testCreateAndFindFilm() {
        Film film = Film.builder().name("Зеленая миля").description("Фантастический эпос о любви и спасении человечества").releaseDate(LocalDate.of(1999, 12, 6)).duration(189).mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей")).build();

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = Optional.of(filmStorage.getById(createdFilm.getId()));

        assertThat(foundFilm).isPresent().hasValueSatisfying(f -> {
            assertThat(f).hasFieldOrPropertyWithValue("id", createdFilm.getId());
            assertThat(f).hasFieldOrPropertyWithValue("name", "Зеленая миля");
            assertThat(f).hasFieldOrPropertyWithValue("description", "Фантастический эпос о любви и спасении человечества");
            assertThat(f).hasFieldOrPropertyWithValue("duration", 189);
            assertThat(f.getMpa()).isNotNull();
            assertThat(f.getMpa().getId()).isEqualTo(2);
        });
    }

    @Test
    public void testUpdateFilm() {
        Film film = Film.builder().name("Начало").description("Психологический триллер Кристофера Нолана").releaseDate(LocalDate.of(2010, 7, 16)).duration(148).mpa(new MpaRating(3, "PG-13", "Просмотр не рекомендуется детям до 13 лет")).build();

        Film createdFilm = filmStorage.create(film);

        Film updatedFilmData = Film.builder().id(createdFilm.getId()).name("Начало (обновленное)").description("Эпическая история о проникновении в сны").releaseDate(LocalDate.of(2010, 7, 16)).duration(150).mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей")).build();

        Film updatedFilm = filmStorage.update(updatedFilmData);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getName()).isEqualTo("Начало (обновленное)");
        assertThat(foundFilm.getDescription()).isEqualTo("Эпическая история о проникновении в сны");
        assertThat(foundFilm.getDuration()).isEqualTo(150);
        assertThat(foundFilm.getMpa().getId()).isEqualTo(2);
    }

    @Test
    public void testCreateFilmWithGenres() {
        Film film = Film.builder().name("Бойцовский клуб").description("Психологический триллер о бунте против системы").releaseDate(LocalDate.of(1999, 9, 10)).duration(139).mpa(new MpaRating(4, "R", "Лица до 17 лет допускаются только в сопровождении родителей")).genres(Set.of(new Genre(2, "Драма"), new Genre(4, "Триллер"))).build();

        Film createdFilm = filmStorage.create(film);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(2, 4);

        assertThat(foundFilm.getGenres()).extracting(Genre::getName).containsExactlyInAnyOrder("Драма", "Триллер");
    }

    @Test
    public void testUpdateFilmWithGenres() {
        Film film = Film.builder().name("Матрица").description("Фантастический боевик о хакере Нео").releaseDate(LocalDate.of(1999, 3, 31)).duration(136).mpa(new MpaRating(3, "PG-13", "Просмотр не рекомендуется детям до 13 лет")).build();

        Film createdFilm = filmStorage.create(film);

        Film updatedFilmData = Film.builder().id(createdFilm.getId()).name("Матрица: Революция").description("Заключительная часть трилогии о Нео и агенте Смите").releaseDate(LocalDate.of(2003, 11, 5)).duration(129).mpa(new MpaRating(3, "PG-13", "Просмотр не рекомендуется детям до 13 лет")).genres(Set.of(new Genre(2, "Драма"), new Genre(6, "Боевик"))).build();

        Film updatedFilm = filmStorage.update(updatedFilmData);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getName()).isEqualTo("Матрица: Революция");
        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(2, 6);
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = Film.builder().name("Побег из Шоушенка").description("История дружбы и надежды в тюрьме Шоушенк").releaseDate(LocalDate.of(1994, 9, 23)).duration(142).mpa(new MpaRating(5, "NC-17", "Лица до 17 лет не допускаются")).build();

        Film film2 = Film.builder().name("Крёстный отец").description("Сага о семье Корлеоне").releaseDate(LocalDate.of(1972, 3, 24)).duration(175).mpa(new MpaRating(4, "R", "Лица до 17 лет допускаются только в сопровождении родителей")).build();

        filmStorage.create(film1);
        filmStorage.create(film2);

        var films = filmStorage.getAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testFilmLikes() {
        int userId1 = createUser("ivanov@example.com", "ivanov", "Иван Иванов", LocalDate.of(1985, 5, 15));
        int userId2 = createUser("petrova@example.com", "petrova", "Мария Петрова", LocalDate.of(1990, 12, 3));

        Film film = Film.builder().name("Титаник").description("Романтическая драма о любви на борту легендарного корабля").releaseDate(LocalDate.of(1997, 12, 19)).duration(194).mpa(new MpaRating(3, "PG-13", "Просмотр не рекомендуется детям до 13 лет")).build();

        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), userId1);
        filmStorage.addLike(createdFilm.getId(), userId2);

        Film filmWithLikes = filmStorage.getById(createdFilm.getId());
        assertThat(filmWithLikes.getLikes()).hasSize(2);
        assertThat(filmWithLikes.getLikes()).containsExactlyInAnyOrder(userId1, userId2);

        filmStorage.removeLike(createdFilm.getId(), userId1);
        Film filmAfterRemove = filmStorage.getById(createdFilm.getId());
        assertThat(filmAfterRemove.getLikes()).hasSize(1);
        assertThat(filmAfterRemove.getLikes()).containsExactly(userId2);
    }
}