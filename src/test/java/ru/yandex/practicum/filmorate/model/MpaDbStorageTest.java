package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.mpa.impl.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {MpaDbStorage.class})
class MpaDbStorageTest {

    @Autowired
    private MpaStorage mpaStorage;

    @Test
    void shouldGetAllMpa() {
        List<MpaRating> ratings = mpaStorage.getAll();
        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0)).hasFieldOrPropertyWithValue("id", 1);
        assertThat(ratings.get(0)).hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    void shouldGetMpaById() {
        MpaRating mpa = mpaStorage.getById(3);
        assertThat(mpa).hasFieldOrPropertyWithValue("id", 3);
        assertThat(mpa).hasFieldOrPropertyWithValue("name", "PG-13");
    }
}