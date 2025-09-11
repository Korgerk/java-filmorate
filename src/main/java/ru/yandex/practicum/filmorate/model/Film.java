package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    @JsonIgnore
    final Set<Integer> likes = new HashSet<>();
    int id;
    @NotBlank
    String name;
    @NotBlank
    String description;
    @NotNull
    LocalDate releaseDate;
    @Positive
    int duration;
    Set<Genre> genres = new LinkedHashSet<>();
    @NotNull
    Mpa mpa;

    public void addLike(Integer id) {
        likes.add(id);
    }

    public void deleteLike(Integer id) {
        likes.remove(id);
    }

    public void addGenre(Genre genre) {
        if (genre != null) {
            genres.removeIf(g -> g.getId() == genre.getId());
            genres.add(genre);
        }
    }

    public void removeAllGenres() {
        genres.clear();
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }
}