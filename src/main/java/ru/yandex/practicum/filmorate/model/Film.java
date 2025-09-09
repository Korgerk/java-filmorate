package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@Setter
public class Film {
    @JsonIgnore
    private final Set<Integer> likes = new HashSet<>();

    private int id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Size(max = 200, message = "Description cannot be longer than 200 characters")
    private String description;

    @NotNull(message = "Release date is required")
    @JsonProperty("releaseDate")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be positive")
    @JsonProperty("duration")
    private int duration;

    private Set<Genre> genres = new HashSet<>();

    @NotNull(message = "Mpa rating is required")
    @JsonProperty("mpa")
    private MpaRating mpa;

    public void addLike(Integer id) {
        likes.add(id);
    }

    public void deleteLike(Integer id) {
        likes.remove(id);
    }

    public void addGenre(Genre genre) {
        boolean isDuplicate = genres.stream().anyMatch(g -> g.getId() == genre.getId());

        if (!isDuplicate) {
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