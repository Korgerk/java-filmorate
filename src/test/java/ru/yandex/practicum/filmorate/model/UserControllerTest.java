package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUserWithBadEmail_shouldShowErrorMessage() {
        User user = User.builder()
                .email("invalid-email")
                .login("petya_petrov")
                .name("Петр Петров")
                .birthday(LocalDate.of(1985, 5, 15))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithEmptyLogin_shouldShowErrorMessage() {
        User user = User.builder()
                .email("valid@example.com")
                .login(" ")
                .name("Анна Смирнова")
                .birthday(LocalDate.of(1990, 8, 22))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithNullLogin_shouldShowErrorMessage() {
        User user = User.builder()
                .email("valid2@example.com")
                .login(null)
                .name("Иван Иванов")
                .birthday(LocalDate.of(1975, 3, 10))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFutureBirthUser_shouldShowErrorMessage() {
        User user = User.builder()
                .email("future.kid@example.com")
                .login("baby_future")
                .name("Малыш Будущий")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserToEmptyEmail_shouldShowErrorMessage() {
        // Сначала создаём пользователя
        User usr = User.builder()
                .email("sasha@yandex.ru")
                .login("sashajaaa")
                .name("Александр")
                .birthday(LocalDate.of(1988, 1, 15))
                .build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();

        // Пытаемся обновить на пустой email
        User user2 = createdUser.toBuilder()
                .email("")
                .build();

        HttpEntity<User> entity = new HttpEntity<>(user2);
        ResponseEntity<User> response2 = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void updateUserToEmptyLogin_shouldShowErrorMessage() {
        // Сначала создаём пользователя
        User usr = User.builder()
                .email("user@yandex.ru")
                .login("user_login")
                .name("Пользователь")
                .birthday(LocalDate.of(1995, 6, 20))
                .build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();

        // Пытаемся обновить на пустой login
        User user = createdUser.toBuilder()
                .login(" ")
                .build();

        HttpEntity<User> entity = new HttpEntity<>(user);
        ResponseEntity<User> response2 = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void updateFutureBirthUser_shouldShowErrorMessage() {
        // Сначала создаём пользователя
        User usr = User.builder()
                .email("user@yandex.ru")
                .login("user_login")
                .name("Пользователь")
                .birthday(LocalDate.of(1995, 6, 20))
                .build();
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", usr, User.class);
        User createdUser = createResponse.getBody();

        // Пытаемся обновить на дату рождения в будущем
        User user = createdUser.toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();

        HttpEntity<User> entity = new HttpEntity<>(user);
        ResponseEntity<User> response2 = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(BAD_REQUEST, response2.getStatusCode());
    }
}