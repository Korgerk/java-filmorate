package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email не может быть пустым")));
    }

    @Test
    public void shouldNotValidateInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("user@example.com")));
    }

    @Test
    public void shouldNotValidateLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("user name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("пробелов")));
    }

    @Test
    public void shouldNotValidateFutureBirthday() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("в будущем")));
    }

    @Test
    public void shouldValidateCorrectUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("login");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}