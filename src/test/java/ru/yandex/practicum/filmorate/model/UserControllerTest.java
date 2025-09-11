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
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUserWithBadEmail_shouldReturnBadRequest() {
        User user = User.builder().email("invalid-email-format").login("alex_smith").name("Алексей Смит").birthday(LocalDate.of(1985, 3, 15)).build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithEmptyLogin_shouldReturnBadRequest() {
        User user = User.builder().email("maria.petrova@example.com").login(" ").name("Мария Петрова").birthday(LocalDate.of(1990, 7, 22)).build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFutureBirthUser_shouldReturnBadRequest() {
        User user = User.builder().email("future.kid@example.com").login("baby_future").name("Малыш Будущее").birthday(LocalDate.now().plusDays(1)).build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserToEmptyEmail_shouldReturnBadRequest() {
        User usr = User.builder().email("ivan.ivanov@example.com").login("ivan_ivanov").name("Иван Иванов").birthday(LocalDate.of(1988, 1, 15)).build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();
        assertNotNull(createdUser, "Созданный пользователь не должен быть null");
        assertNotNull(createdUser.getId(), "ID пользователя должен быть сгенерирован");

        User userToUpdate = createdUser.toBuilder().email("").build();

        HttpEntity<User> entity = new HttpEntity<>(userToUpdate);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserToEmptyLogin_shouldReturnBadRequest() {
        User usr = User.builder().email("anna.smirnova@example.com").login("anna_smirnova").name("Анна Смирнова").birthday(LocalDate.of(1992, 5, 10)).build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();
        assertNotNull(createdUser, "Созданный пользователь не должен быть null");
        assertNotNull(createdUser.getId(), "ID пользователя должен быть сгенерирован");

        User userToUpdate = createdUser.toBuilder().login(" ").build();

        HttpEntity<User> entity = new HttpEntity<>(userToUpdate);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateFutureBirthUser_shouldReturnBadRequest() {
        User usr = User.builder().email("dmitry.kozlov@example.com").login("dmitry_kozlov").name("Дмитрий Козлов").birthday(LocalDate.of(1980, 11, 30)).build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();
        assertNotNull(createdUser, "Созданный пользователь не должен быть null");
        assertNotNull(createdUser.getId(), "ID пользователя должен быть сгенерирован");

        User userToUpdate = createdUser.toBuilder().birthday(LocalDate.now().plusDays(1)).build();

        HttpEntity<User> entity = new HttpEntity<>(userToUpdate);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}