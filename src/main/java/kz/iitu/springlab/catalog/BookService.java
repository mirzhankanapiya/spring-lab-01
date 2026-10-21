package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        return repository.findAll().stream()
                .filter(b -> b.author().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book getById(long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        boolean exists = repository.findAll().stream().anyMatch(b ->
                b.title().equalsIgnoreCase(book.title())
                        && b.author().equalsIgnoreCase(book.author()));
        if (exists) {
            throw new DuplicateTitleException(book.title(), book.author());
        }
        return repository.save(book);
    }

    public Optional<Book> update(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        Book updated = new Book(id, book.title(), book.author(), book.year());
        return Optional.of(repository.save(updated));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    public List<Book> createBulk(List<Book> books) {
        return books.stream()
                .map(repository::save)
                .toList();
    }
}