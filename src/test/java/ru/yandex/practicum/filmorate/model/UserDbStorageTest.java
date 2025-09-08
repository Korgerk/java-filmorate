package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {UserDbStorage.class})
class UserDbStorageTest {

    public static User createTestUser(String email, String login, String name, LocalDate birthday) {
        return User.builder()
                .email(email)
                .login(login)
                .name(name)
                .birthday(birthday)
                .build();
    }

    @Autowired
    private UserStorage userStorage;

    @Test
    void shouldCreateAndFindUserById() {
        User user = User.builder().email("test@example.com").login("testlogin").name("Test User").birthday(LocalDate.of(1990, 1, 1)).build();

        User created = userStorage.create(user);
        User found = userStorage.getById(created.getId());

        assertThat(found).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(created);
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder().email("before@update.com").login("before").name("Before").birthday(LocalDate.of(1990, 1, 1)).build();

        User created = userStorage.create(user);
        created.setName("After");
        created.setLogin("after");
        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("After");
        assertThat(updated.getLogin()).isEqualTo("after");
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = User.builder().email("u1@example.com").login("u1").birthday(LocalDate.of(1990, 1, 1)).build();

        User user2 = User.builder().email("u2@example.com").login("u2").birthday(LocalDate.of(1990, 1, 1)).build();

        userStorage.create(user1);
        userStorage.create(user2);

        Set<User> all = userStorage.getAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userStorage.create(User.builder().email("u1@example.com").login("u1").birthday(LocalDate.of(1990, 1, 1)).build());

        User user2 = userStorage.create(User.builder().email("u2@example.com").login("u2").birthday(LocalDate.of(1990, 1, 1)).build());

        userStorage.addFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1).contains(user2);

        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userStorage.create(User.builder()
                .email("u1@example.com")
                .login("u1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .email("u2@example.com")
                .login("u2")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user3 = userStorage.create(User.builder()
                .email("u3@example.com")
                .login("u3")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        Set<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).hasSize(1).contains(user3);
    }
}