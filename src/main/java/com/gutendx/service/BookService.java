package com.gutendx.service;

import com.gutendx.dto.GutendxApiResponse;
import com.gutendx.entity.Author;
import com.gutendx.entity.Book;
import com.gutendx.exception.ApiException;
import com.gutendx.exception.BookNotFoundException;
import com.gutendx.repository.AuthorRepository;
import com.gutendx.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(BookService.class);

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr", "pt");
    private static final Map<String, String> LANGUAGE_NAMES = Map.of(
            "en", "ingles",
            "es", "español",
            "fr", "frances",
            "pt", "portugues",
            "other", "otro idioma"
    );

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GutendxApiService apiService;

    public Book searchAndSaveBook(String title) throws ApiException, BookNotFoundException {
        logger.info("Buscando y guardando libro: {}", title);

        // Buscar en la API
        GutendxApiResponse apiResponse = apiService.searchBooks(title);

        if (apiResponse.getResults() == null || apiResponse.getResults().isEmpty()) {
            throw new BookNotFoundException("No se encontraron libros para la búsqueda: " + title);
        }

        // Tomar el primer resultado
        GutendxApiResponse.BookDto bookDto = apiResponse.getResults().get(0);

        // Verificar si ya existe en la BD
        Optional<Book> existingBook = bookRepository.findByGutendxBookId(bookDto.getId());
        if (existingBook.isPresent()) {
            logger.info("El libro ya existe en la BD: {}", bookDto.getTitle());
            return existingBook.get();
        }

        // Crear nuevo libro
        Book book = createBookFromDto(bookDto);

        // Procesar autores
        Set<Author> authors = processAuthors(bookDto.getAuthors());
        for (Author author : authors) {
            book.addAuthor(author);
        }

        Book savedBook = bookRepository.save(book);
        logger.info("Libro guardado exitosamente: {}", savedBook.getTitle());

        return savedBook;
    }

    private Book createBookFromDto(GutendxApiResponse.BookDto bookDto) {
        String languageCode = determineLanguageCode(bookDto.getLanguages());

        return new Book(
                bookDto.getId(),
                bookDto.getTitle(),
                languageCode,
                bookDto.getDownloadCount()
        );
    }

    private String determineLanguageCode(List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return "other";
        }

        // Buscar el primer idioma soportado
        return languages.stream()
                .filter(SUPPORTED_LANGUAGES::contains)
                .findFirst()
                .orElse("other");
    }

    private Set<Author> processAuthors(List<GutendxApiResponse.AuthorDto> authorDtos) {
        if (authorDtos == null) {
            return new HashSet<>();
        }

        Set<Author> authors = new HashSet<>();

        for (GutendxApiResponse.AuthorDto authorDto : authorDtos) {
            Author author = findOrCreateAuthor(authorDto);
            authors.add(author);
        }

        return authors;
    }

    private Author findOrCreateAuthor(GutendxApiResponse.AuthorDto authorDto) {
        // Buscar autor existente por nombre original
        Optional<Author> existingAuthor = authorRepository.findByGutendxAuthorName(authorDto.getName());

        if (existingAuthor.isPresent()) {
            // Actualizar datos si es necesario
            Author author = existingAuthor.get();
            if (author.getBirthYear() == null && authorDto.getBirthYear() != null) {
                author.setBirthYear(authorDto.getBirthYear());
            }
            if (author.getDeathYear() == null && authorDto.getDeathYear() != null) {
                author.setDeathYear(authorDto.getDeathYear());
            }
            return authorRepository.save(author);
        }

        // Crear nuevo autor
        String[] names = parseAuthorName(authorDto.getName());
        Author newAuthor = new Author(
                authorDto.getName(),
                names[0], // lastName
                names[1], // firstName
                authorDto.getBirthYear(),
                authorDto.getDeathYear()
        );

        return authorRepository.save(newAuthor);
    }

    private String[] parseAuthorName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{null, null};
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return new String[]{parts[0], null}; // Solo apellido
        } else if (parts.length == 2) {
            return new String[]{parts[1], parts[0]}; // Apellido, Nombre
        } else {
            // Más de dos partes: último como apellido, resto como nombre
            String lastName = parts[parts.length - 1];
            String firstName = String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1));
            return new String[]{lastName, firstName};
        }
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooksOrderedByCreatedAt() {
        return bookRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByLanguage(String languageCode) {
        return bookRepository.findByLanguageCodeOrderByCreatedAtDesc(languageCode);
    }

    @Transactional(readOnly = true)
    public List<Author> getAllAuthors() {
        return authorRepository.findAllWithBooks();
    }

    @Transactional(readOnly = true)
    public List<Author> getAuthorsAliveInYear(int year) {
        return authorRepository.findAuthorsAliveInYear(year);
    }

    @Transactional(readOnly = true)
    public List<Book> getTop3BooksByAuthor(Long authorId) {
        return bookRepository.findByAuthorIdOrderByDownloadCountDesc(authorId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    public String getLanguageName(String languageCode) {
        return LANGUAGE_NAMES.getOrDefault(languageCode, "idioma desconocido");
    }

    public boolean isLanguageSupported(String languageCode) {
        return SUPPORTED_LANGUAGES.contains(languageCode);
    }
}