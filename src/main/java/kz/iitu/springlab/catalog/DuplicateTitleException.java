package kz.iitu.springlab.catalog;

public class DuplicateTitleException extends RuntimeException {
    private final String title;

    public DuplicateTitleException(String title, String author) {
        super("Book '" + title + "' by " + author + " already exists");
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}