package kz.iitu.springlab.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookNotFoundException;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Books", description = "The catalogue of books")
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;
    private final BookMapper mapper;

    public BookRestController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "List books, optionally filtered by author")
    @ApiResponse(responseCode = "200", description = "The list of books")
    @GetMapping
    public List<BookResponse> list(
            @Parameter(description = "Part of the author's name", example = "Bloch")
            @RequestParam(required = false) String author,
            @Parameter(description = "Maximum number of books to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<Book> books = service.findAll(author).stream().limit(limit).toList();
        return mapper.toResponseList(books);
    }

    @Operation(summary = "Find a book by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was found"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public BookResponse find(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id) {
        return mapper.toResponse(service.getById(id));
    }

    @Operation(summary = "Create a book")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "The book was created; the Location header holds its address"),
            @ApiResponse(responseCode = "400", description = "The body breaks a validation rule",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A book with this title and author already exists",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book saved = service.create(mapper.toDomain(request));
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(mapper.toResponse(saved));
    }

    @Operation(summary = "Replace a book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was replaced"),
            @ApiResponse(responseCode = "400", description = "The body breaks a validation rule",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public BookResponse update(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id,
            @Valid @RequestBody BookRequest request) {
        Book updated = service.update(id, mapper.toDomain(request))
                .orElseThrow(() -> new BookNotFoundException(id));
        return mapper.toResponse(updated);
    }

    @Operation(summary = "Delete a book")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The book was deleted"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id) {
        if (!service.delete(id)) {
            throw new BookNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }

    // Lab 5, variant 7: bulk creation
    @Operation(summary = "Create several books at once")
    @ApiResponse(responseCode = "201", description = "The books were created")
    @PostMapping("/bulk")
    public ResponseEntity<List<BookResponse>> createBulk(
            @RequestBody List<@Valid BookRequest> requests) {
        List<Book> books = requests.stream().map(mapper::toDomain).toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponseList(service.createBulk(books)));
    }
}