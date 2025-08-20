package com.gutendx.controller;

import com.gutendx.entity.Author;
import com.gutendx.entity.Book;
import com.gutendx.exception.ApiException;
import com.gutendx.exception.BookNotFoundException;
import com.gutendx.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class ConsoleController implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleController.class);

    @Autowired
    private BookService bookService;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) {
        System.out.println("=== GUTENDX CONSOLE APP ===");

        boolean running = true;
        while (running) {
            showMenu();

            try {
                int option = Integer.parseInt(scanner.nextLine().trim());

                switch (option) {
                    case 1:
                        searchAndSaveBook();
                        break;
                    case 2:
                        listRegisteredBooks();
                        break;
                    case 3:
                        listRegisteredAuthors();
                        break;
                    case 4:
                        listAuthorsAliveInYear();
                        break;
                    case 5:
                        listBooksByLanguage();
                        break;
                    case 0:
                        running = false;
                        System.out.println("¡Hasta luego!");
                        break;
                    default:
                        System.out.println("Opción no válida. Por favor, seleccione una opción del 0 al 5.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido.");
            } catch (Exception e) {
                logger.error("Error inesperado", e);
                System.out.println("Ocurrió un error inesperado. Intente nuevamente.");
            }

            if (running) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
            }
        }

        // opcional: cerrar scanner cuando la app finaliza
        // scanner.close();
    }

    private void showMenu() {
        System.out.println("\n=== GUTENDX CONSOLE APP ===");
        System.out.println("1) Buscar libro por título (y registrar en DB)");
        System.out.println("2) Listar libros registrados");
        System.out.println("3) Listar autores registrados");
        System.out.println("4) Listar autores vivos en un año");
        System.out.println("5) Listar libros por idioma");
        System.out.println("0) Salir");
        System.out.print("Ingrese opción: ");
    }

    private void searchAndSaveBook() {
        System.out.print("\nIngrese el título del libro a buscar: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("El título no puede estar vacío.");
            return;
        }

        try {
            System.out.println("Buscando libro...");
            Book book = bookService.searchAndSaveBook(title);

            // Verificar si ya existía
            boolean alreadyExisted = bookService.getAllBooksOrderedByCreatedAt()
                    .stream()
                    .anyMatch(b -> b.getGutendxBookId().equals(book.getGutendxBookId())
                            && !b.getId().equals(book.getId()));

            if (alreadyExisted) {
                System.out.println("El libro ya existe en la base de datos. Mostrando registro:");
            } else {
                System.out.println("Libro encontrado y registrado:");
            }

            // Mostrar información del libro
            System.out.println("Titulo del libro: " + book.getTitle());

            // Mostrar primer autor
            if (!book.getAuthors().isEmpty()) {
                Author firstAuthor = book.getAuthors().iterator().next();
                System.out.println("Autor: " + firstAuthor.getFormattedName());
            }

            System.out.println("Idioma del libro: " + bookService.getLanguageName(book.getLanguageCode()));
            System.out.println("Número de descargas: " + book.getDownloadCount());

        } catch (BookNotFoundException e) {
            System.out.println("No se encontraron libros para la búsqueda: " + title);
        } catch (ApiException e) {
            System.out.println("Error al consultar la API: " + e.getMessage());
            logger.error("Error API", e);
        } catch (Exception e) {
            // captura de respaldo por si ocurre algo inesperado
            logger.error("Error inesperado en searchAndSaveBook", e);
            System.out.println("Ocurrió un error inesperado. Intente nuevamente.");
        }
    }

    private void listRegisteredBooks() {
        System.out.println("\n=== LIBROS REGISTRADOS ===");

        List<Book> books = bookService.getAllBooksOrderedByCreatedAt();

        if (books.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }

        for (Book book : books) {
            String authorsNames = book.getAuthors().stream()
                    .map(Author::getFormattedName)
                    .collect(Collectors.joining("; "));

            System.out.println("[" + book.getTitle() + "] — Autor(es): " + authorsNames +
                    "; Idioma: " + bookService.getLanguageName(book.getLanguageCode()) +
                    "; Descargas: " + book.getDownloadCount());
        }
    }

    private void listRegisteredAuthors() {
        System.out.println("\n=== AUTORES REGISTRADOS ===");

        List<Author> authors = bookService.getAllAuthors();

        if (authors.isEmpty()) {
            System.out.println("No hay autores registrados.");
            return;
        }

        for (Author author : authors) {
            System.out.println("Autor: " + author.getFormattedName());
            System.out.println("Fecha de nacimiento: " + author.getBirthStatus());
            System.out.println("Fecha de fallecimiento: " + author.getDeathStatus());

            // Mostrar hasta 3 libros más famosos
            List<Book> topBooks = bookService.getTop3BooksByAuthor(author.getId());
            if (!topBooks.isEmpty()) {
                String bookTitles = topBooks.stream()
                        .map(Book::getTitle)
                        .collect(Collectors.joining(", "));
                System.out.println("Libros: " + bookTitles);
            } else {
                System.out.println("Libros: Ninguno registrado");
            }
            System.out.println();
        }
    }

    private void listAuthorsAliveInYear() {
        System.out.print("\nIngrese el año: ");

        try {
            int year = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("\n=== AUTORES VIVOS EN " + year + " ===");

            List<Author> aliveAuthors = bookService.getAuthorsAliveInYear(year);

            if (aliveAuthors.isEmpty()) {
                System.out.println("No se encontraron autores vivos en el año " + year + ".");
                return;
            }

            for (Author author : aliveAuthors) {
                System.out.println("Autor: " + author.getFormattedName());
                System.out.println("Fecha de nacimiento: " + author.getBirthStatus());
                System.out.println("Fecha de fallecimiento: " + author.getDeathStatus());

                // Mostrar hasta 3 libros más famosos
                List<Book> topBooks = bookService.getTop3BooksByAuthor(author.getId());
                if (!topBooks.isEmpty()) {
                    String bookTitles = topBooks.stream()
                            .map(Book::getTitle)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: " + bookTitles);
                } else {
                    System.out.println("Libros: Ninguno registrado");
                }
                System.out.println();
            }

        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un año válido.");
        }
    }

    private void listBooksByLanguage() {
        System.out.println("\nSeleccione idioma: (es - Español, en - Ingles, fr - Frances, pt - Portugues)");
        System.out.print("Ingrese código de idioma: ");

        String languageCode = scanner.nextLine().trim().toLowerCase();

        if (!bookService.isLanguageSupported(languageCode)) {
            System.out.println("Idioma no soportado. Opciones válidas: es, en, fr, pt");
            return;
        }

        System.out.println("\n=== LIBROS EN " + bookService.getLanguageName(languageCode).toUpperCase() + " ===");

        List<Book> books = bookService.getBooksByLanguage(languageCode);

        if (books.isEmpty()) {
            System.out.println("No hay libros registrados en " + bookService.getLanguageName(languageCode) + ".");
            return;
        }

        for (Book book : books) {
            String authorsNames = book.getAuthors().stream()
                    .map(Author::getFormattedName)
                    .collect(Collectors.joining("; "));

            System.out.println("[" + book.getTitle() + "] — Autor(es): " + authorsNames +
                    "; Idioma: " + bookService.getLanguageName(book.getLanguageCode()) +
                    "; Descargas: " + book.getDownloadCount());
        }
    }
}
