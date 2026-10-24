package library;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryServiceImpl {
    private final BookDAO bookDAO = DAOFactory.getBookDAO();
    private final MemberDAO memberDAO = DAOFactory.getMemberDAO();
    private final TransactionDAO transactionDAO = DAOFactory.getTransactionDAO();

    // Books
    public Book registerBook(Book b) throws Exceptions.LibraryException {
        if (bookDAO.findByIsbn(b.getIsbn()).isPresent()) {
            throw new Exceptions.LibraryException("Book with ISBN " + b.getIsbn() + " already exists.");
        }
        return bookDAO.save(b);
    }
    public boolean updateBook(Book b) throws Exceptions.LibraryException { return bookDAO.update(b); }
    public boolean deleteBook(int id) throws Exceptions.LibraryException {
        Book b = getBook(id);
        if (!b.isAvailable()) throw new Exceptions.LibraryException("Book is currently issued.");
        return bookDAO.delete(id);
    }
    public Book getBook(int id) throws Exceptions.LibraryException {
        return bookDAO.findById(id).orElseThrow(() -> new Exceptions.BookNotFoundException("Book not found."));
    }
    public List<Book> getAllBooks() throws Exceptions.LibraryException { return bookDAO.findAll(); }
    public List<Book> searchBooks(String q) throws Exceptions.LibraryException { return bookDAO.search(q); }

    // Members
    public Member registerMember(Member m) throws Exceptions.LibraryException {
        if (memberDAO.findByEmail(m.getEmail()).isPresent()) {
            throw new Exceptions.LibraryException("Member with email already exists.");
        }
        return memberDAO.save(m);
    }
    public boolean updateMember(Member m) throws Exceptions.LibraryException { return memberDAO.update(m); }
    public boolean deleteMember(int id) throws Exceptions.LibraryException {
        if (memberDAO.getActiveBorrowCount(id) > 0) throw new Exceptions.LibraryException("Member has unreturned books.");
        return memberDAO.delete(id);
    }
    public Member getMember(int id) throws Exceptions.LibraryException {
        return memberDAO.findById(id).orElseThrow(() -> new Exceptions.MemberNotFoundException("Member not found."));
    }
    public List<Member> getAllMembers() throws Exceptions.LibraryException { return memberDAO.findAll(); }
    public List<Member> searchMembers(String q) throws Exceptions.LibraryException { return memberDAO.search(q); }

    // Issue Book (ACID Transaction)
    public Transaction issueBook(int bookId, int memberId) throws Exceptions.LibraryException {
        Book book = getBook(bookId);
        if (!book.isAvailable()) throw new Exceptions.BookNotAvailableException("Book is already issued!");

        Member member = getMember(memberId);
        if (!member.isActive()) throw new Exceptions.LibraryException("Member account is suspended!");

        if (memberDAO.getActiveBorrowCount(memberId) >= member.getMaxBorrowLimit()) {
            throw new Exceptions.MaxBorrowLimitExceededException("Member reached borrow limit of " + member.getMaxBorrowLimit());
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(DBConnection.getBorrowDurationDays());
        Transaction tx = new Transaction(0, bookId, memberId, issueDate, dueDate, null, 0.0, false, TransactionStatus.ISSUED);
        tx.setBookTitle(book.getTitle());
        tx.setMemberName(member.getName());

        if (DAOFactory.isUsingInMemory()) {
            transactionDAO.save(tx);
            bookDAO.updateAvailability(bookId, false);
            return tx;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction
            Transaction saved = transactionDAO.save(conn, tx);
            bookDAO.updateAvailability(conn, bookId, false);
            conn.commit();
            saved.setBookTitle(book.getTitle());
            saved.setMemberName(member.getName());
            return saved;
        } catch (SQLException | Exceptions.DatabaseException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new Exceptions.DatabaseException("Issue book failed (Rolled back): " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Return Book (Overdue Fine Calculation with ChronoUnit)
    public Transaction returnBook(int bookId) throws Exceptions.LibraryException {
        Book book = getBook(bookId);
        Transaction tx = transactionDAO.findActiveTransactionByBookId(bookId)
                .orElseThrow(() -> new Exceptions.LibraryException("No active borrow record for Book #" + bookId));

        LocalDate returnDate = LocalDate.now();
        tx.setReturnDate(returnDate);

        // Calculate Overdue Days using ChronoUnit
        long overdueDays = ChronoUnit.DAYS.between(tx.getDueDate(), returnDate);
        double fine = overdueDays > 0 ? (overdueDays * DBConnection.getFinePerDay()) : 0.0;
        tx.setFineAmount(fine);
        tx.setStatus(TransactionStatus.RETURNED);

        if (DAOFactory.isUsingInMemory()) {
            transactionDAO.update(tx);
            bookDAO.updateAvailability(bookId, true);
            return tx;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction
            transactionDAO.update(conn, tx);
            bookDAO.updateAvailability(conn, bookId, true);
            conn.commit();
            return tx;
        } catch (SQLException | Exceptions.DatabaseException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new Exceptions.DatabaseException("Return book failed (Rolled back): " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean payFine(int txId) throws Exceptions.LibraryException {
        return transactionDAO.markFineAsPaid(txId);
    }

    public List<Transaction> getActiveLoans() throws Exceptions.LibraryException { return transactionDAO.findActiveTransactions(); }
    public List<Transaction> getOverdueLoans() throws Exceptions.LibraryException { return transactionDAO.findOverdueTransactions(); }
    public List<Transaction> getAllTransactions() throws Exceptions.LibraryException { return transactionDAO.findAll(); }
    public List<Transaction> getMemberHistory(int memberId) throws Exceptions.LibraryException { return transactionDAO.findByMemberId(memberId); }
}
