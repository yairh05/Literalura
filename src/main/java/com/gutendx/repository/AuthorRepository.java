package com.gutendx.repository;

import com.gutendx.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByGutendxAuthorName(String gutendxAuthorName);

    List<Author> findAllByOrderByLastNameAsc();

    @Query("SELECT a FROM Author a WHERE a.birthYear IS NOT NULL AND a.birthYear <= :year " +
            "AND (a.deathYear IS NULL OR a.deathYear >= :year) ORDER BY a.lastName")
    List<Author> findAuthorsAliveInYear(@Param("year") int year);

    @Query("SELECT DISTINCT a FROM Author a JOIN FETCH a.books ORDER BY a.lastName")
    List<Author> findAllWithBooks();
}
