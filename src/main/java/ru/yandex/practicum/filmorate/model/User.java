package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@FieldDefaults(level = PRIVATE)
public class User {
    Integer id;

    @Email(message = "Email не может быть пустым.")
    @NotBlank(message = "Email не может быть пустым.")
    String email;

    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелов.")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    @NotNull(message = "Дата рождения обязательна.")
    LocalDate birthday;
}