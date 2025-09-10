package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @EqualsAndHashCode.Include
    @Positive(message = "Genre ID must be positive")
    private int id;

    @NotBlank(message = "Genre name cannot be blank")
    private String name;
}