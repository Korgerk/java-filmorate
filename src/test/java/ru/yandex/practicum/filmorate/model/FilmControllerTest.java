package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private MpaRating getDefaultMpa() {
        return MpaRating.builder().id(1)
                .name("G").build();
    }

    @Test
    void createFilmWithEmptyName_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder().name("").description("Научно-фантастический боевик о будущем.").releaseDate(LocalDate.of(2023, 1, 1)).duration(150).mpa(mpa).build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithNullName_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder().name(null).description("Фэнтези о путешествии через порталы.").releaseDate(LocalDate.of(2001, 12, 19)).duration(178).mpa(mpa).build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithMinusDuration_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder().name("Тестовый фильм").description("Фильм с негативной продолжительностью — ошибка.").releaseDate(LocalDate.of(2020, 6, 15)).duration(-100).mpa(mpa).build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilmWithReleaseDateInFuture_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();

        Film film = Film.builder().name("Фильм будущего").description("Этот фильм ещё не снят.").releaseDate(LocalDate.now().plusDays(1)).duration(120).mpa(mpa).build();

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithEmptyName_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder().name("Оригинальное название").description("Изначальное описание фильма.").releaseDate(LocalDate.of(2022, 3, 22)).duration(120).mpa(mpa).build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();
        assertNotNull(createdFilm, "Созданный фильм не должен быть null");
        assertNotNull(createdFilm.getId(), "ID фильма должен быть сгенерирован");

        Film filmToUpdate = createdFilm.toBuilder().name("").build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithNullName_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder().name("Старое название").description("Описание для обновления.").releaseDate(LocalDate.of(2021, 7, 4)).duration(110).mpa(mpa).build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();
        assertNotNull(createdFilm, "Созданный фильм не должен быть null");
        assertNotNull(createdFilm.getId(), "ID фильма должен быть сгенерирован");

        Film filmToUpdate = createdFilm.toBuilder().name(null).build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFilmWithMinusDuration_shouldReturnBadRequest() {
        MpaRating mpa = getDefaultMpa();
        Film filmToCreate = Film.builder().name("Длинный фильм").description("Фильм, который нужно обновить.").releaseDate(LocalDate.of(2019, 11, 11)).duration(180).mpa(mpa).build();

        ResponseEntity<Film> createResponse = restTemplate.postForEntity("/films", filmToCreate, Film.class);
        Film createdFilm = createResponse.getBody();
        assertNotNull(createdFilm, "Созданный фильм не должен быть null");
        assertNotNull(createdFilm.getId(), "ID фильма должен быть сгенерирован");

        Film filmToUpdate = createdFilm.toBuilder().duration(-45).build();

        HttpEntity<Film> entity = new HttpEntity<>(filmToUpdate);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}