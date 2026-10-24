package library;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public interface BookDAO {
    Book save(Book book) throws Exceptions.DatabaseException;
    boolean update(Book book) throws Exceptions.DatabaseException;
    boolean delete(int bookId) throws Exceptions.DatabaseException;
    Optional<Book> findById(int bookId) throws Exceptions.DatabaseException;
    Optional<Book> findByIsbn(String isbn) throws Exceptions.DatabaseException;
    List<Book> findAll() throws Exceptions.DatabaseException;
    List<Book> search(String query) throws Exceptions.DatabaseException;
    boolean updateAvailability(int bookId, boolean available) throws Exceptions.DatabaseException;
    boolean updateAvailability(Connection conn, int bookId, boolean available) throws Exceptions.DatabaseException;
}
