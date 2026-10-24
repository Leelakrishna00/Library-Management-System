package library;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {
    Transaction save(Transaction transaction) throws Exceptions.DatabaseException;
    Transaction save(Connection conn, Transaction transaction) throws Exceptions.DatabaseException;
    boolean update(Transaction transaction) throws Exceptions.DatabaseException;
    boolean update(Connection conn, Transaction transaction) throws Exceptions.DatabaseException;
    Optional<Transaction> findById(int transactionId) throws Exceptions.DatabaseException;
    Optional<Transaction> findActiveTransactionByBookId(int bookId) throws Exceptions.DatabaseException;
    List<Transaction> findByMemberId(int memberId) throws Exceptions.DatabaseException;
    List<Transaction> findAll() throws Exceptions.DatabaseException;
    List<Transaction> findActiveTransactions() throws Exceptions.DatabaseException;
    List<Transaction> findOverdueTransactions() throws Exceptions.DatabaseException;
    boolean markFineAsPaid(int transactionId) throws Exceptions.DatabaseException;
}
