package kz.iitu.springlab.web.dto;

import java.util.List;

import kz.iitu.springlab.catalog.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toDomain(BookRequest request) {
        return new Book(request.title(), request.genre(), request.year());
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getGenre(), book.getYear());
    }

    public List<BookResponse> toResponseList(List<Book> books) {
        return books.stream().map(this::toResponse).toList();
    }
}