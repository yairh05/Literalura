package com.gutendx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gutendx_author_name", nullable = false, unique = true)
    private String gutendxAuthorName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "death_year")
    private Integer deathYear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public Author() {}

    public Author(String gutendxAuthorName, String lastName, String firstName,
                  Integer birthYear, Integer deathYear) {
        this.gutendxAuthorName = gutendxAuthorName;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getFormattedName() {
        if (lastName != null && firstName != null) {
            return lastName + ", " + firstName;
        } else if (lastName != null) {
            return lastName;
        } else if (firstName != null) {
            return firstName;
        } else {
            return gutendxAuthorName;
        }
    }

    public boolean isAliveInYear(int year) {
        if (birthYear == null) {
            return false; // No se puede determinar si no conocemos fecha de nacimiento
        }

        if (birthYear > year) {
            return false; // Aún no había nacido
        }

        return deathYear == null || deathYear >= year;
    }

    public String getDeathStatus() {
        return deathYear == null ? "Sigue vivo" : deathYear.toString();
    }

    public String getBirthStatus() {
        return birthYear == null ? "Desconocida" : birthYear.toString();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGutendxAuthorName() { return gutendxAuthorName; }
    public void setGutendxAuthorName(String gutendxAuthorName) { this.gutendxAuthorName = gutendxAuthorName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public Integer getDeathYear() { return deathYear; }
    public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<Book> getBooks() { return books; }
    public void setBooks(Set<Book> books) { this.books = books; }
}
