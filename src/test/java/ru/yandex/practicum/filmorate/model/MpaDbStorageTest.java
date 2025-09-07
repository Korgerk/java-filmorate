package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {MpaDbStorage.class})
class MpaDbStorageTest {
    @Autowired
    private MpaStorage mpaStorage;

    @Test
    void testGetAllMpaRatings() {
        List<MpaRating> ratings = mpaStorage.getAll();

        assertThat(ratings).hasSize(5);
        assertThat(ratings).extracting(MpaRating::getName).contains("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testGetMpaRatingById() {
        MpaRating rating = mpaStorage.getById(1);

        assertThat(rating).isNotNull();
        assertThat(rating.getId()).isEqualTo(1);
        assertThat(rating.getName()).isEqualTo("G");
    }
}