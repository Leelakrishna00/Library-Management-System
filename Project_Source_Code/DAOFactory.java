package library;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DAOFactory {
    private static boolean useInMemory = false;
    private static BookDAO bookDAO;
    private static MemberDAO memberDAO;
    private static TransactionDAO transactionDAO;

    static {
        if (DBConnection.testConnection()) {
            useInMemory = false;
            bookDAO = new BookDAOImpl();
            memberDAO = new MemberDAOImpl();
            transactionDAO = new TransactionDAOImpl();
        } else {
            useInMemory = true;
            setupInMemory();
        }
    }

    private static void setupInMemory() {
        Map<Integer, Book> books = new ConcurrentHashMap<>();
        Map<Integer, Member> members = new ConcurrentHashMap<>();
        Map<Integer, Transaction> txs = new ConcurrentHashMap<>();
        AtomicInteger bId = new AtomicInteger(10);
        AtomicInteger mId = new AtomicInteger(5);
        AtomicInteger tId = new AtomicInteger(3);

        // Seed Books
        books.put(1, new Book(1, "978-0134685991", "Effective Java", "Joshua Bloch", "Computer Science", 2018, false));
        books.put(2, new Book(2, "978-0132350884", "Clean Code", "Robert C. Martin", "Computer Science", 2008, false));
        books.put(3, new Book(3, "978-0596009205", "Head First Design Patterns", "Eric Freeman", "Computer Science", 2004, true));
        books.put(4, new Book(4, "978-0321356680", "Java Concurrency in Practice", "Brian Goetz", "Computer Science", 2006, true));
        books.put(5, new Book(5, "978-0141439518", "Pride and Prejudice", "Jane Austen", "Literature", 1813, true));
        books.put(6, new Book(6, "978-0451524935", "1984", "George Orwell", "Dystopian Fiction", 1949, true));

        // Seed Members
        members.put(1, new Member(1, "Aarav Sharma", "aarav@example.com", "9876543210", LocalDate.now().minusMonths(3), 3, "ACTIVE"));
        members.put(2, new Member(2, "Diya Patel", "diya@example.com", "9876543211", LocalDate.now().minusMonths(2), 3, "ACTIVE"));
        members.put(3, new Member(3, "Rohan Verma", "rohan@example.com", "9876543212", LocalDate.now().minusMonths(1), 3, "ACTIVE"));

        // Seed Transactions
        Transaction t1 = new Transaction(1, 1, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, 0.0, false, TransactionStatus.ISSUED);
        t1.setBookTitle("Effective Java");
        t1.setMemberName("Aarav Sharma");
        txs.put(1, t1);

        Transaction t2 = new Transaction(2, 2, 2, LocalDate.now().minusDays(18), LocalDate.now().minusDays(4), null, 8.0, false, TransactionStatus.ISSUED);
        t2.setBookTitle("Clean Code");
        t2.setMemberName("Diya Patel");
        txs.put(2, t2);

        bookDAO = new BookDAO() {
            public Book save(Book b) { if (b.getBookId()<=0) b.setBookId(bId.incrementAndGet()); books.put(b.getBookId(), b); return b; }
            public boolean update(Book b) { books.put(b.getBookId(), b); return true; }
            public boolean delete(int id) { return books.remove(id) != null; }
            public Optional<Book> findById(int id) { return Optional.ofNullable(books.get(id)); }
            public Optional<Book> findByIsbn(String isbn) { return books.values().stream().filter(b->b.getIsbn().equalsIgnoreCase(isbn.trim())).findFirst(); }
            public List<Book> findAll() { return new ArrayList<>(books.values()); }
            public List<Book> search(String q) { return books.values().stream().filter(b->b.getTitle().toLowerCase().contains(q.toLowerCase())||b.getAuthor().toLowerCase().contains(q.toLowerCase())).collect(Collectors.toList()); }
            public boolean updateAvailability(int id, boolean a) { Book b = books.get(id); if (b!=null) { b.setAvailable(a); return true;} return false; }
            public boolean updateAvailability(Connection c, int id, boolean a) { return updateAvailability(id, a); }
        };

        memberDAO = new MemberDAO() {
            public Member save(Member m) { if (m.getId()<=0) m.setId(mId.incrementAndGet()); members.put(m.getId(), m); return m; }
            public boolean update(Member m) { members.put(m.getId(), m); return true; }
            public boolean delete(int id) { return members.remove(id) != null; }
            public Optional<Member> findById(int id) { return Optional.ofNullable(members.get(id)); }
            public Optional<Member> findByEmail(String e) { return members.values().stream().filter(m->m.getEmail().equalsIgnoreCase(e.trim())).findFirst(); }
            public List<Member> findAll() { return new ArrayList<>(members.values()); }
            public List<Member> search(String q) { return members.values().stream().filter(m->m.getName().toLowerCase().contains(q.toLowerCase())).collect(Collectors.toList()); }
            public int getActiveBorrowCount(int id) { return (int)txs.values().stream().filter(t->t.getMemberId()==id && t.getStatus()==TransactionStatus.ISSUED).count(); }
        };

        transactionDAO = new TransactionDAO() {
            public Transaction save(Transaction t) { if (t.getTransactionId()<=0) t.setTransactionId(tId.incrementAndGet()); txs.put(t.getTransactionId(), t); return t; }
            public Transaction save(Connection c, Transaction t) { return save(t); }
            public boolean update(Transaction t) { txs.put(t.getTransactionId(), t); return true; }
            public boolean update(Connection c, Transaction t) { return update(t); }
            public Optional<Transaction> findById(int id) { return Optional.ofNullable(txs.get(id)); }
            public Optional<Transaction> findActiveTransactionByBookId(int bId) { return txs.values().stream().filter(t->t.getBookId()==bId && t.getStatus()==TransactionStatus.ISSUED).findFirst(); }
            public List<Transaction> findByMemberId(int mId) { return txs.values().stream().filter(t->t.getMemberId()==mId).collect(Collectors.toList()); }
            public List<Transaction> findAll() { return new ArrayList<>(txs.values()); }
            public List<Transaction> findActiveTransactions() { return txs.values().stream().filter(t->t.getStatus()==TransactionStatus.ISSUED).collect(Collectors.toList()); }
            public List<Transaction> findOverdueTransactions() { return txs.values().stream().filter(t->t.getStatus()==TransactionStatus.ISSUED && t.getDueDate().isBefore(LocalDate.now())).collect(Collectors.toList()); }
            public boolean markFineAsPaid(int id) { Transaction t = txs.get(id); if (t!=null) { t.setFinePaid(true); return true;} return false; }
        };
    }

    public static boolean isUsingInMemory() { return useInMemory; }
    public static BookDAO getBookDAO() { return bookDAO; }
    public static MemberDAO getMemberDAO() { return memberDAO; }
    public static TransactionDAO getTransactionDAO() { return transactionDAO; }
}
