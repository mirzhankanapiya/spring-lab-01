package kz.iitu.springlab.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Book> listWithAuthors() {
        return repository.findAllBy();
    }

    @Transactional(readOnly = true)
    public Page<Book> find(Genre genre, Pageable pageable) {
        return genre == null
                ? repository.findAll(pageable)
                : repository.findByGenre(genre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Book getById(long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Book getWithAuthor(long id) {
        return repository.findWithAuthorById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book create(Book book) {
        if (repository.existsByTitleIgnoreCase(book.getTitle())) {
            throw new DuplicateTitleException(book.getTitle());
        }
        return repository.save(book);
    }

    @Transactional
    public Optional<Book> update(long id, Book data) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(data.getTitle());
            existing.setGenre(data.getGenre());
            existing.setYear(data.getYear());
            return existing;   // save() не нужен: сущность управляется контекстом
        });
    }

    @Transactional
    public boolean delete(long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Transactional
    public List<Book> createBulk(List<Book> books) {
        return repository.saveAll(books);
    }
}