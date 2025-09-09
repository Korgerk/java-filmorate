package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private int id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Size(max = 200, message = "Description cannot be longer than 200 characters")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Duration must be positive")
    private int duration;

    private MpaRating mpa;

    private Set<Genre> genres = new HashSet<>();

    private Set<Integer> likes = new HashSet<>();
}