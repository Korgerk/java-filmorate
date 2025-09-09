package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.expectation.NotFoundException;
import ru.yandex.practicum.filmorate.expectation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
@Transactional
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        validate(user, "User form is filled in incorrectly");
        User result = userStorage.create(user);
        return result;
    }

    public User update(User user) {
        validate(user, "User update form is filled in incorrectly");
        if (getById(user.getId()) == null) {
            throw new NotFoundException("User with ID = " + user.getId() + " not found");
        }
        User result = userStorage.update(user);
        return result;
    }

    public void delete(int userId) {
        if (getById(userId) == null) {
            throw new NotFoundException("User with ID = " + userId + " not found");
        }
        log.info("Deleted film with id: {}", userId);
        userStorage.delete(userId);
    }

    public User getById(Integer id) {
        log.info("Requested user with ID = " + id);
        return userStorage.getById(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        checkUser(userId, friendId);

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        userStorage.addFriend(userId, friendId);

        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        checkUser(userId, friendId);

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        userStorage.removeFriend(userId, friendId);

        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getAllFriends(Integer userId) {
        checkUser(userId, userId);
        List<User> result = userStorage.getFriends(userId);
        log.info("Friends of user with ID = " + userId + result);
        return result;
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        checkUser(user1Id, user2Id);
        List<User> result = userStorage.getCommonFriends(user1Id, user2Id);
        log.info("Common friends of users with ID " + " {} and {} {} ", user1Id, user2Id, result);
        return result;
    }

    private void checkUser(Integer userId, Integer friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
    }

    private void validate(User user, String message) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug(message);
            throw new ValidationException(message);
        }
    }

    private void preSave(User user) {
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public List<User> getFriends(int userId) {
        User user = getById(userId);
        return user.getFriendIds().stream().map(friendId -> getById(friendId)).collect(Collectors.toList());
    }
}