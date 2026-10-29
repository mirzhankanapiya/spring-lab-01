package kz.iitu.springlab.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//@Component
@Profile("!prod")
@Order(2)
public class QueryDemo implements CommandLineRunner {

    private final BookRepository books;

    public QueryDemo(BookRepository books) {
        this.books = books;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("=== findByGenre");
        System.out.println(books.findByGenre(Genre.PROGRAMMING).size());

        System.out.println("=== findByTitleContainingIgnoreCase");
        System.out.println(books.findByTitleContainingIgnoreCase("clean").size());

        System.out.println("=== findByYearBetweenOrderByYearDesc");
        System.out.println(books.findByYearBetweenOrderByYearDesc(1940, 2010).size());

        System.out.println("=== findTop3ByGenreOrderByYearDesc");
        System.out.println(books.findTop3ByGenreOrderByYearDesc(Genre.FANTASY).size());

        System.out.println("=== countByAuthorName");
        System.out.println(books.countByAuthorName("Robert C. Martin"));

        System.out.println("=== search (JPQL)");
        System.out.println(books.search(Genre.PROGRAMMING, 2010).size());

        System.out.println("=== N+1 BEFORE (findAll)");
        books.findAll().forEach(b -> System.out.println(b.getAuthor().getName()));

        System.out.println("=== N+1 AFTER (join fetch)");
        books.findAllWithAuthor().forEach(b -> System.out.println(b.getAuthor().getName()));

        System.out.println("=== archiveOlderThan (bulk)");
        System.out.println(books.archiveOlderThan(1950));
        System.out.println("=== END");
    }
}