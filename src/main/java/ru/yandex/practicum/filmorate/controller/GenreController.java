package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT * FROM genres ORDER BY id");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM genres WHERE id = ?", (rs, rowNum) -> {
            Map<String, Object> genre = new HashMap<>();
            genre.put("id", rs.getInt("id"));
            genre.put("name", rs.getString("name"));
            return genre;
        }, id);
    }
}