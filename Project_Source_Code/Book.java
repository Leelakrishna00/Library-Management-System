package library;

public class Book implements Comparable<Book> {
    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int publishedYear;
    private boolean available;

    public Book() {
        this.available = true;
    }

    public Book(int bookId, String isbn, String title, String author, String category, int publishedYear, boolean available) {
        this.bookId = bookId;
        setIsbn(isbn);
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        this.publishedYear = publishedYear;
        this.available = available;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) throw new IllegalArgumentException("ISBN cannot be empty.");
        this.isbn = isbn.trim();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title cannot be empty.");
        this.title = title.trim();
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) throw new IllegalArgumentException("Author cannot be empty.");
        this.author = author.trim();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public int getPublishedYear() { return publishedYear; }
    public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public int compareTo(Book other) {
        if (other == null) return 1;
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s by %s | ISBN: %s | Genre: %s | Year: %d | Status: %s",
                bookId, title, author, isbn, category, publishedYear, (available ? "AVAILABLE" : "ISSUED"));
    }
}
