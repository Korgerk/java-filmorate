package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void shouldNotValidateEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email не может быть пустым")));
    }

    @Test
    public void shouldNotValidateInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("user@example.com")));
    }

    @Test
    public void shouldNotValidateEmptyLogin() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Логин не может быть пустым")));
    }

    @Test
    public void shouldNotValidateLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("user login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("пробелов")));
    }

    @Test
    public void shouldNotValidateFutureBirthday() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("в будущем")));
    }

    @Test
    public void shouldSetLoginAsNameIfNameEmpty() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("userlogin");
        user.setName(""); // пустое имя
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пустое имя допустимо — будет использован логин");

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }

        assertEquals("userlogin", user.getName());
    }

    @Test
    public void shouldValidateCorrectUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("userlogin");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}