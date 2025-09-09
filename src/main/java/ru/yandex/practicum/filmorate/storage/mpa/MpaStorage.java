package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface MpaStorage {
    MpaRating getRatingMpaById(int ratingId);

    List<MpaRating> getRatingsMpa();
}