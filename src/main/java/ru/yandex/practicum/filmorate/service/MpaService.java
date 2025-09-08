package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class MpaService {

    private MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<MpaRating> getAllMpa() {
        return mpaStorage.getAllMpa().stream().sorted(Comparator.comparing(MpaRating::getId)).collect(Collectors.toList());
    }

    public MpaRating getMpaById(Integer id) {
        return mpaStorage.getMpaById(id);
    }
}
