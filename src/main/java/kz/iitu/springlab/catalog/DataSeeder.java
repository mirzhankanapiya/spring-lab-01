package kz.iitu.springlab.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod")
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final AuthorRepository authors;
    private final TagRepository tags;

    public DataSeeder(AuthorRepository authors, TagRepository tags) {
        this.authors = authors;
        this.tags = tags;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (authors.count() > 0) {
            return;
        }

        Tag programming = tags.save(new Tag("programming"));
        Tag classic = tags.save(new Tag("classic"));
        Tag fantasy = tags.save(new Tag("fantasy"));
        Tag dystopia = tags.save(new Tag("dystopia"));

        Author martin = new Author("Robert C. Martin", "USA");
        Book cleanCode = new Book("Clean Code", Genre.PROGRAMMING, 2008);
        cleanCode.addTag(programming);
        cleanCode.addTag(classic);                  // книга с двумя тегами
        Book cleanArch = new Book("Clean Architecture", Genre.PROGRAMMING, 2017);
        cleanArch.addTag(programming);
        Book coder = new Book("The Clean Coder", Genre.PROGRAMMING, 2011);
        coder.addTag(programming);
        martin.addBook(cleanCode);
        martin.addBook(cleanArch);
        martin.addBook(coder);                      // автор с тремя книгами

        Author tolkien = new Author("J. R. R. Tolkien", "UK");
        Book hobbit = new Book("The Hobbit", Genre.FANTASY, 1937);
        hobbit.addTag(fantasy);
        hobbit.addTag(classic);
        Book fellowship = new Book("The Fellowship of the Ring", Genre.FANTASY, 1954);
        fellowship.addTag(fantasy);
        Book towers = new Book("The Two Towers", Genre.FANTASY, 1954);
        towers.addTag(fantasy);
        tolkien.addBook(hobbit);
        tolkien.addBook(fellowship);
        tolkien.addBook(towers);

        Author orwell = new Author("George Orwell", "UK");
        Book nineteen = new Book("1984", Genre.FICTION, 1949);
        nineteen.addTag(dystopia);
        nineteen.addTag(classic);
        Book farm = new Book("Animal Farm", Genre.FICTION, 1945);
        farm.addTag(dystopia);
        orwell.addBook(nineteen);
        orwell.addBook(farm);

        authors.save(martin);     // книги сохранятся каскадом (CascadeType.ALL)
        authors.save(tolkien);
        authors.save(orwell);
    }
}