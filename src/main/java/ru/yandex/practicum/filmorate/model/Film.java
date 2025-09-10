package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@Setter
public class Film {
    @JsonIgnore
    private final Set<Integer> likes = new HashSet<>();
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Set<Genre> genres = new LinkedHashSet<>();
    @NotNull
    private Mpa mpa;

    public void addLike(Integer id) {
        likes.add(id);
    }

    public void deleteLike(Integer id) {
        likes.remove(id);
    }

    public void addGenre(Genre genre) {
        if (genre != null) {
            boolean alreadyExists = genres.stream()
                    .anyMatch(g -> g.getId() == genre.getId());

            if (!alreadyExists) {
                genres.add(genre);
            }
        }
    }

    public void removeAllGenres() {
        genres.clear();
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }
}