package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaStorage mpaStorage;

    @Test
    void testGetAllMpaRatings() {
        List<MpaRating> mpaList = mpaStorage.getAll();

        assertThat(mpaList).isNotNull();
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList).extracting(MpaRating::getName).contains("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testGetMpaById1() {
        MpaRating mpa = mpaStorage.getById(1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void testGetMpaById2() {
        MpaRating mpa = mpaStorage.getById(2);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(2);
        assertThat(mpa.getName()).isEqualTo("PG");
    }

    @Test
    void testGetMpaById3() {
        MpaRating mpa = mpaStorage.getById(3);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(3);
        assertThat(mpa.getName()).isEqualTo("PG-13");
    }

    @Test
    void testGetMpaById4() {
        MpaRating mpa = mpaStorage.getById(4);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(4);
        assertThat(mpa.getName()).isEqualTo("R");
    }

    @Test
    void testGetMpaById5() {
        MpaRating mpa = mpaStorage.getById(5);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(5);
        assertThat(mpa.getName()).isEqualTo("NC-17");
    }

    @Test
    void testGetMpaByIdNotFound() {
        assertThrows(ValidationException.class, () -> mpaStorage.getById(999));
    }

    @Test
    void testMpaOrder() {
        List<MpaRating> mpaList = mpaStorage.getAll();

        // Проверяем, что рейтинги отсортированы по ID
        for (int i = 0; i < mpaList.size() - 1; i++) {
            assertThat(mpaList.get(i).getId()).isLessThan(mpaList.get(i + 1).getId());
        }
    }
}