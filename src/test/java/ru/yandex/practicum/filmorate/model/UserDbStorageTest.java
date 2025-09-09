package ru.yandex.practicum.filmorate.model;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testCreateAndFindUser() {
        User user = User.builder().email("alexey.smirnov@mail.ru").login("alex_smirnov").name("Алексей Смирнов").birthday(LocalDate.of(1992, 8, 21)).build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("alexey.smirnov@mail.ru");
        assertThat(foundUser.getLogin()).isEqualTo("alex_smirnov");
        assertThat(foundUser.getName()).isEqualTo("Алексей Смирнов");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(1992, 8, 21));
    }

    @Test
    public void testUpdateUser() {
        User user = User.builder().email("olga.petrova@gmail.com").login("olga_petrova").name("Ольга Петрова").birthday(LocalDate.of(1988, 3, 12)).build();

        User createdUser = userStorage.create(user);

        User updatedUserData = User.builder().id(createdUser.getId()).email("olga.ivanova@gmail.com").login("olga_ivanova").name("Ольга Иванова").birthday(LocalDate.of(1988, 3, 12)).build();

        User updatedUser = userStorage.update(updatedUserData);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Ольга Иванова");
        assertThat(foundUser.getEmail()).isEqualTo("olga.ivanova@gmail.com");
        assertThat(foundUser.getLogin()).isEqualTo("olga_ivanova");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(1988, 3, 12));
    }

    @Test
    public void testGetAllUsers() {
        User user1 = User.builder().email("dmitry.kozlov@yandex.ru").login("dmitry_k").name("Дмитрий Козлов").birthday(LocalDate.of(1985, 11, 15)).build();

        User user2 = User.builder().email("ekaterina.vasilieva@gmail.com").login("katya_v").name("Екатерина Васильева").birthday(LocalDate.of(1990, 4, 28)).build();

        userStorage.create(user1);
        userStorage.create(user2);

        assertThat(userStorage.getAll()).hasSize(2);
    }

    @Test
    public void testGetById() {
        User user = User.builder().email("maria.sokolova@outlook.com").login("maria_s").name("Мария Соколова").birthday(LocalDate.of(1995, 7, 9)).build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("maria.sokolova@outlook.com");
        assertThat(foundUser.getLogin()).isEqualTo("maria_s");
    }

    @Test
    public void testUserWithEmptyName() {
        User user = User.builder().email("test@example.com").login("testlogin").name("").birthday(LocalDate.of(1990, 1, 1)).build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser.getName()).isEqualTo("testlogin");
    }

    @Test
    public void testUserWithNullName() {
        User user = User.builder().email("test@example.com").login("testlogin").name(null).birthday(LocalDate.of(1990, 1, 1)).build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser.getName()).isEqualTo("testlogin");
    }
}