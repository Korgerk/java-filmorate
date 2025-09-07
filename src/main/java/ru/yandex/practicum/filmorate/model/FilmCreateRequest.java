package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmCreateRequest {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    @Valid
    private MpaDto mpa;
    @Valid
    private List<GenreDto> genres;
}