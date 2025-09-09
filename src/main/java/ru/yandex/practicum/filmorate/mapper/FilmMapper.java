package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.filmHelper.FilmDto;

@Component
public class FilmMapper {

    public Film convertToFilm(FilmDto filmDto) {
        return Film.builder()
                .name(filmDto.getName())
                .description(filmDto.getDescription())
                .releaseDate(filmDto.getReleaseDate())
                .duration(filmDto.getDuration())
                .genres(filmDto.getGenres())
                .mpa(filmDto.getMpa())
                .build();
    }

    public void updateFilmFromRequest(Film film, FilmDto filmDto) {
        film.setName(filmDto.getName());
        film.setDescription(filmDto.getDescription());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDuration(filmDto.getDuration());
        film.setGenres(filmDto.getGenres());
        film.setMpa(filmDto.getMpa());
    }
}