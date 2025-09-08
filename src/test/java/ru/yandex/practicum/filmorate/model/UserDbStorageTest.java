package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDbStorage.class)
public class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldCreateAndFindUser() {
        User user = User.builder().email("test@example.com").login("testuser").name("Test User").birthday(LocalDate.of(1990, 1, 1)).build();

        User created = userStorage.createUser(user);
        Optional<User> found = userStorage.findUserById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder().email("old@example.com").login("oldlogin").name("Old").birthday(LocalDate.now()).build();

        User created = userStorage.createUser(user);
        created.setName("Updated");
        created.setEmail("new@example.com");

        User updated = userStorage.updateUser(created);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldAddAndConfirmFriend() {
        User user1 = userStorage.createUser(User.builder().email("u1@example.com").login("u1").birthday(LocalDate.now()).build());
        User user2 = userStorage.createUser(User.builder().email("u2@example.com").login("u2").birthday(LocalDate.now()).build());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());

        userStorage.confirmFriend(user2.getId(), user1.getId());

        List<User> friends = userStorage.getUserFriends(user2.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user1.getId());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userStorage.createUser(User.builder().email("u1@example.com").login("u1").birthday(LocalDate.now()).build());
        User user2 = userStorage.createUser(User.builder().email("u2@example.com").login("u2").birthday(LocalDate.now()).build());
        User common = userStorage.createUser(User.builder().email("common@example.com").login("common").birthday(LocalDate.now()).build());

        userStorage.addFriend(common.getId(), user1.getId());
        userStorage.addFriend(user1.getId(), common.getId());

        userStorage.confirmFriend(user1.getId(), common.getId());

        userStorage.addFriend(common.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        userStorage.confirmFriend(user2.getId(), common.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(common.getId());
    }
}