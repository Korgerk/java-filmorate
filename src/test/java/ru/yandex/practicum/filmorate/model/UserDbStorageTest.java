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

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1990, 1, 1));

        user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateAndFindUserById() {
        User createdUser = userStorage.create(user1);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("user1@example.com");

        User foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(createdUser);
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(user1);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(user1);
        userStorage.create(user2);

        Set<User> allUsers = userStorage.getAll();
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail).contains("user1@example.com", "user2@example.com");
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = userStorage.create(this.user1);
        User user2 = userStorage.create(this.user2);

        userStorage.addFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends).extracting(User::getId).contains(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());

        friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.create(this.user1);
        User user2 = userStorage.create(this.user2);
        User user3 = userStorage.create(this.user3);

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        Set<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends).extracting(User::getId).contains(user3.getId());
    }

    @Test
    void testUserNotFound() {
        int nonExistentId = 999;
        assertThrows(ValidationException.class, () -> userStorage.getById(nonExistentId));
    }
}