-- MPA Ratings
MERGE INTO mpa_ratings (id, name) VALUES (1, 'G');
MERGE INTO mpa_ratings (id, name) VALUES (2, 'PG');
MERGE INTO mpa_ratings (id, name) VALUES (3, 'PG-13');
MERGE INTO mpa_ratings (id, name) VALUES (4, 'R');
MERGE INTO mpa_ratings (id, name) VALUES (5, 'NC-17');

-- Genres
MERGE INTO genres (id, name) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) VALUES (2, 'Драма');
MERGE INTO genres (id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) VALUES (6, 'Боевик');

-- Test Users
MERGE INTO users (id, email, login, name, birthday) VALUES (1, 'user1@yandex.ru', 'user1', 'User One', '1990-01-01');
MERGE INTO users (id, email, login, name, birthday) VALUES (8, 'user8@yandex.ru', 'user8', 'User Eight', '1990-01-01');
MERGE INTO users (id, email, login, name, birthday) VALUES (9, 'user9@yandex.ru', 'user9', 'User Nine', '1990-01-01');