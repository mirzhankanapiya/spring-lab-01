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
import kz.iitu.springlab.catalog.Genre;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Books", description = "CRUD operations on books")
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;
    private final BookMapper mapper;

    public BookRestController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "List books, paged and sorted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of books")
    })
    @GetMapping
    public Page<BookResponse> list(
            @Parameter(description = "Filter by genre", example = "PROGRAMMING")
            @RequestParam(required = false) Genre genre,
            @PageableDefault(size = 5, sort = "title") Pageable pageable) {
        return service.find(genre, pageable).map(mapper::toResponse);
    }

    @Operation(summary = "Find a book by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was found"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public EntityModel<BookResponse> find(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id) {
        BookResponse book = mapper.toResponse(service.getById(id));
        return EntityModel.of(book,
                linkTo(methodOn(BookRestController.class).find(id)).withSelfRel(),
                linkTo(methodOn(BookRestController.class).list(null, null)).withRel("books"));
    }

    @Operation(summary = "Create a book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The book was created"),
            @ApiResponse(responseCode = "400", description = "The body is not valid",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The book already exists",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book saved = service.create(mapper.toDomain(request));
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.getId()))
                .body(mapper.toResponse(saved));
    }

    @Operation(summary = "Replace a book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was replaced"),
            @ApiResponse(responseCode = "400", description = "The body is not valid",
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

    @Operation(summary = "Create several books at once")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The books were created")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<BookResponse>> createBulk(
            @RequestBody List<@Valid BookRequest> requests) {
        List<Book> books = requests.stream().map(mapper::toDomain).toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponseList(service.createBulk(books)));
    }
}