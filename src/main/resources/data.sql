INSERT INTO mpa_ratings (id, name) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

INSERT INTO genres (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

-- Для тестов
INSERT INTO users (id, email, login, name, birthday) VALUES
(1, 'user1@yandex.ru', 'user1', 'User One', '1990-01-01'),
(2, 'user2@yandex.ru', 'user2', 'User Two', '1990-01-01');