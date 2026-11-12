package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookScrollRepository;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.ScrollResponse;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books/scroll")
public class BookScrollController {

    private final BookScrollRepository repository;
    private final BookMapper mapper;

    public BookScrollController(BookScrollRepository repository, BookMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // Keyset: WHERE (year, id) is after the last seen row, no OFFSET
    @GetMapping("/keyset")
    public ScrollResponse keyset(@RequestParam(defaultValue = "0") int fromYear,
                                 @RequestParam(defaultValue = "3") int size,
                                 @RequestParam(required = false) String cursor) {
        ScrollPosition position = ScrollPosition.keyset();
        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("_");
                Map<String, Object> keys = new LinkedHashMap<>();
                keys.put("year", Integer.parseInt(parts[0]));
                keys.put("id", Long.parseLong(parts[1]));
                position = ScrollPosition.forward(keys);
            } catch (RuntimeException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor");
            }
        }

        Window<Book> window = repository.findByYearGreaterThanEqualOrderByYearDescIdAsc(
                fromYear, position, Limit.of(size));

        List<Book> content = window.getContent();
        String next = null;
        if (window.hasNext() && !content.isEmpty()) {
            Book last = content.get(content.size() - 1);
            next = last.getYear() + "_" + last.getId();
        }
        return new ScrollResponse(mapper.toResponseList(content), next, window.hasNext());
    }

    // Offset: the same Window, but the database skips rows with OFFSET
    @GetMapping("/offset")
    public ScrollResponse offset(@RequestParam(defaultValue = "0") int fromYear,
                                 @RequestParam(defaultValue = "3") int size,
                                 @RequestParam(required = false) Long cursor) {
        ScrollPosition position = cursor == null
                ? ScrollPosition.offset()
                : ScrollPosition.offset(cursor);

        Window<Book> window = repository.findByYearGreaterThanEqualOrderByYearDescIdAsc(
                fromYear, position, Limit.of(size));

        String next = null;
        if (window.hasNext() && window.size() > 0) {
            OffsetScrollPosition last =
                    (OffsetScrollPosition) window.positionAt(window.size() - 1);
            next = String.valueOf(last.getOffset());
        }
        return new ScrollResponse(mapper.toResponseList(window.getContent()), next, window.hasNext());
    }
}