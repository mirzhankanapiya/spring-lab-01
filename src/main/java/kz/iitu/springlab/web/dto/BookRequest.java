package kz.iitu.springlab.web.dto;

import jakarta.validation.constraints.*;
import kz.iitu.springlab.catalog.Genre;

public record BookRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull Genre genre,
        @Min(1450) @Max(2100) int year) { }