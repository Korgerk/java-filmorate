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
@RequestMapping("/mpa")
public class MpaController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT * FROM mpa_ratings ORDER BY id");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM mpa_ratings WHERE id = ?", (rs, rowNum) -> {
            Map<String, Object> mpa = new HashMap<>();
            mpa.put("id", rs.getInt("id"));
            mpa.put("name", rs.getString("name"));
            return mpa;
        }, id);
    }
}