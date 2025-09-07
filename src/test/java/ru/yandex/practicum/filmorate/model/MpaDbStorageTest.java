package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaStorage mpaStorage;

    @Test
    void testGetAllMpa() {
        List<MpaRating> mpaList = mpaStorage.getAll();
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList.get(0).getName()).isEqualTo("G");
    }

    @Test
    void testGetMpaById() {
        MpaRating mpa = mpaStorage.getById(1);
        assertThat(mpa).isNotNull();
        assertThat(mpa.getName()).isEqualTo("G");
    }
}