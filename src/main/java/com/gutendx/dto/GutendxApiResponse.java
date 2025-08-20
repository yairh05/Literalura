package com.gutendx.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public class GutendxApiResponse {

    private int count;
    private String next;
    private String previous;
    private List<BookDto> results;

    public GutendxApiResponse() {}

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<BookDto> getResults() { return results; }
    public void setResults(List<BookDto> results) { this.results = results; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDto {

        private Long id;
        private String title;
        private List<AuthorDto> authors;
        private List<String> languages;

        @JsonProperty("download_count")
        private Integer downloadCount;

        public BookDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public List<AuthorDto> getAuthors() { return authors; }
        public void setAuthors(List<AuthorDto> authors) { this.authors = authors; }

        public List<String> getLanguages() { return languages; }
        public void setLanguages(List<String> languages) { this.languages = languages; }

        public Integer getDownloadCount() { return downloadCount != null ? downloadCount : 0; }
        public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorDto {

        private String name;

        @JsonProperty("birth_year")
        private Integer birthYear;

        @JsonProperty("death_year")
        private Integer deathYear;

        public AuthorDto() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getBirthYear() { return birthYear; }
        public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

        public Integer getDeathYear() { return deathYear; }
        public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }
    }
}
