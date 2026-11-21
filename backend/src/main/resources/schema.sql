DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS training_sessions;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT 1
);

CREATE TABLE training_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    trainer_id INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL DEFAULT 6,
    FOREIGN KEY (trainer_id) REFERENCES users(id)
);

CREATE TABLE enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    training_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    UNIQUE (training_id, student_id),
    FOREIGN KEY (training_id) REFERENCES training_sessions(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trainer_id INTEGER NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (trainer_id) REFERENCES users(id)
);

