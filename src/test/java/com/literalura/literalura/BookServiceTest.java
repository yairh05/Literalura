package com.literalura.literalura;

import com.gutendx.dto.GutendxApiResponse;
import com.gutendx.entity.Author;
import com.gutendx.entity.Book;
import com.gutendx.exception.ApiException;
import com.gutendx.exception.BookNotFoundException;
import com.gutendx.repository.AuthorRepository;
import com.gutendx.repository.BookRepository;
import com.gutendx.service.BookService;
import com.gutendx.service.GutendxApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GutendxApiService apiService;

    @InjectMocks
    private BookService bookService;

    private GutendxApiResponse mockApiResponse;
    private GutendxApiResponse.BookDto mockBookDto;
    private GutendxApiResponse.AuthorDto mockAuthorDto;

    @BeforeEach
    void setUp() {
        // Configurar mock de respuesta de API
        mockApiResponse = new GutendxApiResponse();
        mockBookDto = new GutendxApiResponse.BookDto();
        mockBookDto.setId(1L);
        mockBookDto.setTitle("Test Book");
        mockBookDto.setLanguages(List.of("en"));
        mockBookDto.setDownloadCount(1000);

        mockAuthorDto = new GutendxApiResponse.AuthorDto();
        mockAuthorDto.setName("Doe, John");
        mockAuthorDto.setBirthYear(1980);
        mockAuthorDto.setDeathYear(null);

        mockBookDto.setAuthors(List.of(mockAuthorDto));
        mockApiResponse.setResults(List.of(mockBookDto));
    }

    @Test
    void testSearchAndSaveBook_NewBook_Success() throws Exception {
        // Given
        String title = "Test Book";
        when(apiService.searchBooks(title)).thenReturn(mockApiResponse);
        when(bookRepository.findByGutendxBookId(1L)).thenReturn(Optional.empty());
        when(authorRepository.findByGutendxAuthorName("Doe, John")).thenReturn(Optional.empty());

        Author mockAuthor = new Author("Doe, John", "Doe", "John", 1980, null);
        when(authorRepository.save(any(Author.class))).thenReturn(mockAuthor);

        Book mockBook = new Book(1L, "Test Book", "en", 1000);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        // When
        Book result = bookService.searchAndSaveBook(title);

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("en", result.getLanguageCode());
        assertEquals(1000, result.getDownloadCount());

        verify(apiService).searchBooks(title);
        verify(bookRepository).save(any(Book.class));
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void testSearchAndSaveBook_ExistingBook_ReturnsExisting() throws Exception {
        // Given
        String title = "Test Book";
        Book existingBook = new Book(1L, "Test Book", "en", 1000);

        when(apiService.searchBooks(title)).thenReturn(mockApiResponse);
        when(bookRepository.findByGutendxBookId(1L)).thenReturn(Optional.of(existingBook));

        // When
        Book result = bookService.searchAndSaveBook(title);

        // Then
        assertNotNull(result);
        assertEquals(existingBook, result);

        verify(apiService).searchBooks(title);
        verify(bookRepository).findByGutendxBookId(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testSearchAndSaveBook_NoResults_ThrowsException() throws Exception {
        // Given
        String title = "Nonexistent Book";
        GutendxApiResponse emptyResponse = new GutendxApiResponse();
        emptyResponse.setResults(Collections.emptyList());

        when(apiService.searchBooks(title)).thenReturn(emptyResponse);

        // When & Then
        assertThrows(BookNotFoundException.class, () -> {
            bookService.searchAndSaveBook(title);
        });

        verify(apiService).searchBooks(title);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testSearchAndSaveBook_ApiException_ThrowsException() throws Exception {
        // Given
        String title = "Test Book";
        when(apiService.searchBooks(title)).thenThrow(new ApiException("API Error"));

        // When & Then
        assertThrows(ApiException.class, () -> {
            bookService.searchAndSaveBook(title);
        });

        verify(apiService).searchBooks(title);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testGetLanguageName() {
        // Test supported languages
        assertEquals("ingles", bookService.getLanguageName("en"));
        assertEquals("español", bookService.getLanguageName("es"));
        assertEquals("frances", bookService.getLanguageName("fr"));
        assertEquals("portugues", bookService.getLanguageName("pt"));
        assertEquals("otro idioma", bookService.getLanguageName("other"));
        assertEquals("idioma desconocido", bookService.getLanguageName("unknown"));
    }

    @Test
    void testIsLanguageSupported() {
        assertTrue(bookService.isLanguageSupported("en"));
        assertTrue(bookService.isLanguageSupported("es"));
        assertTrue(bookService.isLanguageSupported("fr"));
        assertTrue(bookService.isLanguageSupported("pt"));
        assertFalse(bookService.isLanguageSupported("de"));
        assertFalse(bookService.isLanguageSupported("other"));
    }

    @Test
    void testGetAllBooksOrderedByCreatedAt() {
        // Given
        List<Book> mockBooks = List.of(
                new Book(1L, "Book 1", "en", 1000),
                new Book(2L, "Book 2", "es", 2000)
        );
        when(bookRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockBooks);

        // When
        List<Book> result = bookService.getAllBooksOrderedByCreatedAt();

        // Then
        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("Book 2", result.get(1).getTitle());

        verify(bookRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testGetBooksByLanguage() {
        // Given
        String languageCode = "en";
        List<Book> mockBooks = List.of(new Book(1L, "English Book", "en", 1000));
        when(bookRepository.findByLanguageCodeOrderByCreatedAtDesc(languageCode))
                .thenReturn(mockBooks);

        // When
        List<Book> result = bookService.getBooksByLanguage(languageCode);

        // Then
        assertEquals(1, result.size());
        assertEquals("English Book", result.get(0).getTitle());
        assertEquals("en", result.get(0).getLanguageCode());

        verify(bookRepository).findByLanguageCodeOrderByCreatedAtDesc(languageCode);
    }

    @Test
    void testGetAuthorsAliveInYear() {
        // Given
        int year = 2000;
        List<Author> mockAuthors = List.of(
                new Author("Doe, John", "Doe", "John", 1980, null)
        );
        when(authorRepository.findAuthorsAliveInYear(year)).thenReturn(mockAuthors);

        // When
        List<Author> result = bookService.getAuthorsAliveInYear(year);

        // Then
        assertEquals(1, result.size());
        assertEquals("Doe, John", result.get(0).getGutendxAuthorName());

        verify(authorRepository).findAuthorsAliveInYear(year);
    }
}
