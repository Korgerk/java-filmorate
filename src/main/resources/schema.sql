DROP TABLE IF EXISTS user_friends;
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS mpa_ratings;

CREATE TABLE IF NOT EXISTS mpa_ratings (
    id INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_rating_id INTEGER,
    FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings(id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id INTEGER,
    user_id INTEGER,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id INTEGER,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_friends (
    user_id INTEGER,
    friend_id INTEGER,
    confirmed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);