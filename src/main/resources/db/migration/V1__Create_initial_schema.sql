-- V1__Create_initial_schema.sql
-- Crear tabla de autores
CREATE TABLE author (
    id SERIAL PRIMARY KEY,
    gutendx_author_name TEXT NOT NULL UNIQUE, -- raw name from API
    last_name TEXT,
    first_name TEXT,
    birth_year INTEGER,
    death_year INTEGER,
    created_at TIMESTAMP DEFAULT now()
);

-- Crear tabla de libros
CREATE TABLE book (
    id SERIAL PRIMARY KEY,
    gutendx_book_id INTEGER UNIQUE NOT NULL, -- id from Gutendx to avoid duplicados
    title TEXT NOT NULL,
    language_code VARCHAR(10) NOT NULL, -- 'en', 'es', 'fr', 'pt', 'other'
    download_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT now()
);

-- Crear tabla de relación muchos a muchos entre libros y autores
CREATE TABLE book_author (
    book_id INTEGER REFERENCES book(id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

-- Crear índices para mejorar performance
CREATE INDEX idx_gutendx_book_id ON book(gutendx_book_id);
CREATE INDEX idx_language_code ON book(language_code);
CREATE INDEX idx_author_name ON author(gutendx_author_name);
CREATE INDEX idx_birth_year ON author(birth_year);
CREATE INDEX idx_death_year ON author(death_year);