package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mpa {
    @Positive(message = "MPA ID must be positive")
    private int id;

    @NotBlank(message = "MPA name cannot be blank")
    private String name;
}