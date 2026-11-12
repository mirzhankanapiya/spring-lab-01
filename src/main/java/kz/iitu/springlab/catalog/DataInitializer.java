package kz.iitu.springlab.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository repository;

    public DataInitializer(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.save(new Book("Effective Java", Genre.PROGRAMMING, 2018));
        repository.save(new Book("Clean Code", Genre.PROGRAMMING, 2008));
        // repository.save(new Book("Spring in Action", Genre.PROGRAMMING, 2022));
    }
}