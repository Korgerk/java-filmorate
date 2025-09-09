package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;

    }

    public MpaRating getRatingMpaById(int id) {
        MpaRating mpa = mpaStorage.getRatingMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Rating not found");
        }
        return mpa;
    }

    public List<MpaRating> getRatingsMpa() {
        return mpaStorage.getRatingsMpa();
    }
}