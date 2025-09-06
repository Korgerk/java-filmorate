package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, Film> {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(Film film, ConstraintValidatorContext context) {
        if (film.getReleaseDate() == null) {
            return true;
        }
        return !film.getReleaseDate().isBefore(MIN_RELEASE_DATE);
    }
}