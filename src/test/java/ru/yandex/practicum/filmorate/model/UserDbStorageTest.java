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
    void testCreateUser() {
        User createdUser = userStorage.create(testUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("testuser@yandex.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getName()).isEqualTo("Test User");
    }

    @Test
    void testCreateUserWithEmptyName() {
        testUser.setName("");
        User createdUser = userStorage.create(testUser);

        assertThat(createdUser.getName()).isEqualTo("testuser");
    }

    @Test
    void testCreateUserWithNullName() {
        testUser.setName(null);
        User createdUser = userStorage.create(testUser);

        assertThat(createdUser.getName()).isEqualTo("testuser");
    }

    @Test
    void testGetUserById() {
        User createdUser = userStorage.create(testUser);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("testuser@yandex.ru");
    }

    @Test
    void testGetUserByIdNotFound() {
        assertThrows(ValidationException.class, () -> userStorage.getById(999));
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
    void testUpdateUserNotFound() {
        testUser.setId(999);
        assertThrows(ValidationException.class, () -> userStorage.update(testUser));
    }

    @Test
    void testGetAllUsers() {
        User user = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@yandex.ru");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        userStorage.create(user2);

        Set<User> allUsers = userStorage.getAll();
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUserExists() {
        User createdUser = userStorage.create(testUser);

        assertThat(userStorage.exists(createdUser.getId())).isTrue();
        assertThat(userStorage.exists(999)).isFalse();
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@yandex.ru");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), createdUser2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(createdUser2.getId());

        userStorage.removeFriend(user1.getId(), createdUser2.getId());

        friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testAddFriendUserNotFound() {
        assertThrows(ValidationException.class, () -> userStorage.addFriend(999, 1));
    }

    @Test
    void testAddFriendFriendNotFound() {
        User user = userStorage.create(testUser);
        assertThrows(ValidationException.class, () -> userStorage.addFriend(user.getId(), 999));
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@yandex.ru");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser2 = userStorage.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@yandex.ru");
        commonFriend.setLogin("common");
        commonFriend.setBirthday(LocalDate.of(1993, 1, 1));
        User createdCommonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(user1.getId(), createdCommonFriend.getId());
        userStorage.addFriend(createdUser2.getId(), createdCommonFriend.getId());

        Set<User> commonFriends = userStorage.getCommonFriends(user1.getId(), createdUser2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(createdCommonFriend.getId());
    }

    @Test
    void testGetCommonFriendsNoCommon() {
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@yandex.ru");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser2 = userStorage.create(user2);

        Set<User> commonFriends = userStorage.getCommonFriends(user1.getId(), createdUser2.getId());
        assertThat(commonFriends).isEmpty();
    }

    @Test
    void testGetFriendsEmpty() {
        User user = userStorage.create(testUser);
        Set<User> friends = userStorage.getFriends(user.getId());
        assertThat(friends).isEmpty();
    }
}