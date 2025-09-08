package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // Общий метод для создания MpaRating
    private MpaRating getDefaultMpa() {
        return MpaRating.builder()
                .id(1) // G
                .name("G")
                .build();
    }

    @Test
    void createFilmWithEmptyName_shouldShowErrorMessage() {
        // Добавьте MpaRating здесь
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder()
                .name("")
                .description("Научно-фантастический боевик о будущем.")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(150)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithNullName_shouldShowErrorMessage() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder()
                .name(null)
                .description("Фэнтези о путешествии через порталы.")
                .releaseDate(LocalDate.of(2001, 12, 19))
                .duration(178)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithMinusDuration_shouldShowErrorMessage() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder()
                .name("Тестовый фильм")
                .description("Фильм с негативной продолжительностью — ошибка.")
                .releaseDate(LocalDate.of(2020, 6, 15))
                .duration(-100)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithReleaseDateInFuture_shouldShowErrorMessage() {
        // Добавьте MpaRating здесь
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder()
                .name("Фильм будущего")
                .description("Этот фильм ещё не снят.")
                .releaseDate(LocalDate.now().plusDays(1))
                .duration(120)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithEmptyName_shouldShowErrorMessage() {
        // Сначала создаём валидный фильм (с MPA!)
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder()
                .name("Оригинальное название")
                .description("Изначальное описание фильма.")
                .releaseDate(LocalDate.of(2022, 3, 22))
                .duration(120)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();

        // Теперь пытаемся обновить с пустым именем
        Film filmToUpdate = createdFilm.toBuilder()
                .name("")
                .build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithNullName_shouldShowErrorMessage() {
        // Сначала создаём валидный фильм (с MPA!)
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder()
                .name("Старое название")
                .description("Описание для обновления.")
                .releaseDate(LocalDate.of(2021, 7, 4))
                .duration(110)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();

        // Теперь пытаемся обновить с null именем
        Film filmToUpdate = createdFilm.toBuilder()
                .name(null)
                .build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithMinusDuration_shouldShowErrorMessage() {
        // Сначала создаём валидный фильм (с MPA!)
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder()
                .name("Длинный фильм")
                .description("Фильм, который нужно обновить.")
                .releaseDate(LocalDate.of(2019, 11, 11))
                .duration(180)
                .mpa(mpa)
                .build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();

        // Теперь пытаемся обновить с негативной длительностью
        Film filmToUpdate = createdFilm.toBuilder()
                .duration(-45)
                .build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }
}