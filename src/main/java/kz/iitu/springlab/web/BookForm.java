package kz.iitu.springlab.web;

import jakarta.validation.constraints.*;
import kz.iitu.springlab.catalog.Genre;

public class BookForm {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotNull
    private Genre genre;

    @Min(1450)
    @Max(2100)
    private int year;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}