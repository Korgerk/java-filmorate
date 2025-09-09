package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<Mpa> getRatingsMpa() {
        log.info("Запрос на все MPA");
        return mpaService.getRatingsMpa();
    }

    @GetMapping("/{id}")
    public Mpa getRatingMpaById(@PathVariable Integer id) {
        log.info("Запрос на получение MPA c id {}", id);
        return mpaService.getRatingMpaById(id);
    }
}