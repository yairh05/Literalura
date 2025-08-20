package com.literalura.literalura;


import com.gutendx.entity.Author;
import com.gutendx.entity.Book;
import com.gutendx.repository.AuthorRepository;
import com.gutendx.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void testDatabaseOperations() {
        // Create and save author
        Author author = new Author("Shakespeare, William", "Shakespeare", "William", 1564, 1616);
        Author savedAuthor = authorRepository.save(author);

        assertNotNull(savedAuthor.getId());
        assertEquals("Shakespeare, William", savedAuthor.getGutendxAuthorName());
        assertEquals("Shakespeare", savedAuthor.getLastName());
        assertEquals("William", savedAuthor.getFirstName());

        // Create and save book
        Book book = new Book(1L, "Hamlet", "en", 5000);
        book.addAuthor(savedAuthor);
        Book savedBook = bookRepository.save(book);

        assertNotNull(savedBook.getId());
        assertEquals("Hamlet", savedBook.getTitle());
        assertEquals("en", savedBook.getLanguageCode());
        assertEquals(1, savedBook.getAuthors().size());

        // Test queries
        Optional<Book> foundBook = bookRepository.findByGutendxBookId(1L);
        assertTrue(foundBook.isPresent());
        assertEquals("Hamlet", foundBook.get().getTitle());

        Optional<Author> foundAuthor = authorRepository.findByGutendxAuthorName("Shakespeare, William");
        assertTrue(foundAuthor.isPresent());
        assertEquals("William", foundAuthor.get().getFirstName());

        // Test author alive in year
        List<Author> authorsAliveIn1600 = authorRepository.findAuthorsAliveInYear(1600);
        assertEquals(1, authorsAliveIn1600.size());
        assertEquals("Shakespeare, William", authorsAliveIn1600.get(0).getGutendxAuthorName());

        List<Author> authorsAliveIn1700 = authorRepository.findAuthorsAliveInYear(1700);
        assertEquals(0, authorsAliveIn1700.size());

        // Test books by author
        List<Book> booksByAuthor = bookRepository.findByAuthorIdOrderByDownloadCountDesc(savedAuthor.getId());
        assertEquals(1, booksByAuthor.size());
        assertEquals("Hamlet", booksByAuthor.get(0).getTitle());
    }

    @Test
    void testAuthorNameParsing() {
        Author author = new Author("García Márquez, Gabriel", "García Márquez", "Gabriel", 1927, 2014);
        Author saved = authorRepository.save(author);

        assertEquals("García Márquez, Gabriel", saved.getFormattedName());
        assertTrue(saved.isAliveInYear(1980));
        assertFalse(saved.isAliveInYear(2020));
    }

    @Test
    void testLanguageFiltering() {
        // Save books in different languages
        Book englishBook = new Book(1L, "English Book", "en", 1000);
        Book spanishBook = new Book(2L, "Libro Español", "es", 2000);
        Book frenchBook = new Book(3L, "Livre Français", "fr", 3000);

        bookRepository.save(englishBook);
        bookRepository.save(spanishBook);
        bookRepository.save(frenchBook);

        List<Book> englishBooks = bookRepository.findByLanguageCodeOrderByCreatedAtDesc("en");
        assertEquals(1, englishBooks.size());
        assertEquals("English Book", englishBooks.get(0).getTitle());

        List<Book> spanishBooks = bookRepository.findByLanguageCodeOrderByCreatedAtDesc("es");
        assertEquals(1, spanishBooks.size());
        assertEquals("Libro Español", spanishBooks.get(0).getTitle());
    }
}