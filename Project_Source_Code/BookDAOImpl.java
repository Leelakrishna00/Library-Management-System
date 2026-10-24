package library;

import java.sql.*;
import java.util.*;

public class BookDAOImpl implements BookDAO {
    @Override
    public Book save(Book book) throws Exceptions.DatabaseException {
        String sql = "INSERT INTO books (isbn, title, author, category, published_year, available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setInt(5, book.getPublishedYear());
            stmt.setBoolean(6, book.isAvailable());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) book.setBookId(rs.getInt(1));
            }
            return book;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error saving book: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Book book) throws Exceptions.DatabaseException {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, category=?, published_year=?, available=? WHERE book_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setInt(5, book.getPublishedYear());
            stmt.setBoolean(6, book.isAvailable());
            stmt.setInt(7, book.getBookId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error updating book: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int bookId) throws Exceptions.DatabaseException {
        String sql = "DELETE FROM books WHERE book_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error deleting book: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findById(int bookId) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM books WHERE book_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error finding book: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM books WHERE isbn=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error finding book by ISBN: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> findAll() throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM books ORDER BY title ASC";
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error finding all books: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> search(String query) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ?";
        List<Book> list = new ArrayList<>();
        String q = "%" + query + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q);
            stmt.setString(2, q);
            stmt.setString(3, q);
            stmt.setString(4, q);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error searching books: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateAvailability(int bookId, boolean available) throws Exceptions.DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            return updateAvailability(conn, bookId, available);
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean updateAvailability(Connection conn, int bookId, boolean available) throws Exceptions.DatabaseException {
        String sql = "UPDATE books SET available=? WHERE book_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Transaction book availability error: " + e.getMessage(), e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("published_year"),
                rs.getBoolean("available")
        );
    }
}
