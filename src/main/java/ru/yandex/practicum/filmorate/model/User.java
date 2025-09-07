package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
public class User {
    Integer id;

    @Email(message = "Электронная почта должна быть в формате user@example.com.")
    @NotBlank(message = "Email не может быть пустым.")
    String email;

    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелов.")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    LocalDate birthday;

    // Геттер для имени с логином по умолчанию
    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }

    // Метод для установки имени с логином по умолчанию
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            this.name = this.login;
        } else {
            this.name = name;
        }
    }
}