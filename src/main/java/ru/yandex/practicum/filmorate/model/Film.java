package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
@ValidReleaseDate
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