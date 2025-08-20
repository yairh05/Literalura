package com.gutendx.repository;

import com.gutendx.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByGutendxBookId(Long gutendxBookId);

    List<Book> findByLanguageCodeOrderByCreatedAtDesc(String languageCode);

    List<Book> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId ORDER BY b.downloadCount DESC")
    List<Book> findByAuthorIdOrderByDownloadCountDesc(@Param("authorId") Long authorId);
}
