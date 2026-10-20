package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookNotFoundException;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;
    private final BookMapper mapper;

    public BookRestController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping                                            // 200
    public List<BookResponse> list(@RequestParam(required = false) String author,
                                   @RequestParam(defaultValue = "10") int limit) {
        List<Book> books = service.findAll(author).stream().limit(limit).toList();
        return mapper.toResponseList(books);
    }

    @GetMapping("/{id}")                                   // 200 or 404
    public BookResponse find(@PathVariable long id) {
        return mapper.toResponse(service.getById(id));
    }

    @PostMapping                                           // 201 + Location
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book saved = service.create(mapper.toDomain(request));
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")                                   // 200 or 404
    public BookResponse update(@PathVariable long id,
                               @Valid @RequestBody BookRequest request) {
        Book updated = service.update(id, mapper.toDomain(request))
                .orElseThrow(() -> new BookNotFoundException(id));
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")                                // 204 or 404
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!service.delete(id)) {
            throw new BookNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }

    // Lab 5 variant 7: bulk creation
    @PostMapping("/bulk")                                  // 201
    public ResponseEntity<List<BookResponse>> createBulk(
            @RequestBody List<@Valid BookRequest> requests) {
        List<Book> books = requests.stream().map(mapper::toDomain).toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponseList(service.createBulk(books)));
    }
}