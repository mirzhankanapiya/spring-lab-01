package kz.iitu.springlab.web.dto;

public record BookResponse(
        Long id,
        String title,
        String author,
        int year) { }