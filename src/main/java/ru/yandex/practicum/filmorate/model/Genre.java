package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Genre {
    @EqualsAndHashCode.Include
    @Positive(message = "Genre ID must be positive")
    int id;

    @NotBlank(message = "Genre name cannot be blank")
    String name;

    @Override
    public String toString() {
        return "Genre{id=" + id + ", name='" + name + "'}";
    }
}