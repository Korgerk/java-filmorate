package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем тестовые данные перед каждым тестом
        jdbcTemplate.update("DELETE FROM user_friends");
        jdbcTemplate.update("DELETE FROM users WHERE email LIKE 'test%@yandex.ru'");

        testUser = new User();
        testUser.setEmail("testuser@yandex.ru");
        testUser.setLogin("testuser");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateAndGetUser() {
        // Создание пользователя
        User createdUser = userStorage.create(testUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("testuser@yandex.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getName()).isEqualTo("Test User");

        // Получение пользователя по ID
        User foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo(createdUser.getEmail());
    }

    @Test
    void testUpdateUser() {
        // Создаем пользователя
        User createdUser = userStorage.create(testUser);

        // Обновляем данные
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@yandex.ru");
        createdUser.setLogin("updatedlogin");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@yandex.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("updatedlogin");
    }

    @Test
    void testGetAllUsers() {
        // Создаем несколько пользователей
        userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("testuser2@yandex.ru");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        userStorage.create(user2);

        Set<User> allUsers = userStorage.getAll();
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testAddAndRemoveFriend() {
        // Создаем двух пользователей
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@yandex.ru");
        user2.setLogin("friend");
        user2.setName("Friend User");
        user2.setBirthday(LocalDate.of(1992, 3, 3));
        User user2Created = userStorage.create(user2);

        // Добавляем в друзья
        userStorage.addFriend(user1.getId(), user2Created.getId());

        // Проверяем, что друг добавлен
        Set<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(user2Created.getId());

        // Удаляем из друзей
        userStorage.removeFriend(user1.getId(), user2Created.getId());

        // Проверяем, что друг удален
        friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        // Создаем трех пользователей
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@yandex.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        User user2Created = userStorage.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@yandex.ru");
        commonFriend.setLogin("common");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 3, 3));
        User commonFriendCreated = userStorage.create(commonFriend);

        // Добавляем общего друга
        userStorage.addFriend(user1.getId(), commonFriendCreated.getId());
        userStorage.addFriend(user2Created.getId(), commonFriendCreated.getId());

        // Проверяем общих друзей
        Set<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2Created.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(commonFriendCreated.getId());
    }

    @Test
    void testUserNotFound() {
        assertThrows(ValidationException.class, () -> userStorage.getById(9999));
    }

    @Test
    void testUpdateNonExistentUser() {
        testUser.setId(9999);
        assertThrows(ValidationException.class, () -> userStorage.update(testUser));
    }

    @Test
    void testAddFriendNonExistentUser() {
        User user = userStorage.create(testUser);
        assertThrows(ValidationException.class, () -> userStorage.addFriend(user.getId(), 9999));
    }

    @Test
    void testAddFriendToNonExistentUser() {
        User user = userStorage.create(testUser);
        assertThrows(ValidationException.class, () -> userStorage.addFriend(9999, user.getId()));
    }
}