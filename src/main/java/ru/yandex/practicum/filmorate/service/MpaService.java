package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMpaDbStorage;


import java.util.List;

@Service
public class MpaService {
    private final RatingMpaDbStorage ratingMpaDbStorage;

    public MpaService(RatingMpaDbStorage ratingMpaDbStorage) {
        this.ratingMpaDbStorage = ratingMpaDbStorage;
    }

    public MpaRating getRatingMpaById(int id) {
        MpaRating mpa = ratingMpaDbStorage.getRatingMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Rating not found");
        }
        return mpa;
    }

    public List<MpaRating> getRatingsMpa() {
        return ratingMpaDbStorage.getRatingsMpa();
    }
}