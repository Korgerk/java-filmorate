package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
public class Film {
    Integer id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов.")
    String description;

    @NotNull(message = "Дата релиза обязательна.")
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной.")
    Integer duration;
}