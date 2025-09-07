package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("testuser@yandex.ru");
        testUser.setLogin("testuser");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateAndFindUserById() {
        User createdUser = userStorage.create(testUser);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("testuser@yandex.ru");

        User foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(createdUser);
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(testUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@yandex.ru");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@yandex.ru");
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(testUser);

        Set<User> allUsers = userStorage.getAll();
        // В data.sql уже есть 3 пользователя: id=1,8,9
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allUsers).extracting(User::getEmail).contains("user1@yandex.ru", "user8@yandex.ru", "user9@yandex.ru");
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = userStorage.getById(1); // уже есть в data.sql
        User user2 = userStorage.getById(8);

        userStorage.addFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).extracting(User::getId).contains(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());

        friends = userStorage.getFriends(user1.getId());
        assertThat(friends).extracting(User::getId).doesNotContain(user2.getId());
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.getById(1);
        User user2 = userStorage.getById(8);
        User user3 = userStorage.getById(9);

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        Set<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).extracting(User::getId).contains(user3.getId());
    }

    @Test
    void testUserNotFound() {
        int nonExistentId = 999;
        assertThrows(ValidationException.class, () -> userStorage.getById(nonExistentId));
    }
}