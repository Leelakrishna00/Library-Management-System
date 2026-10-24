package library;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class TransactionDAOImpl implements TransactionDAO {
    private static final String SELECT_JOIN =
            "SELECT t.*, b.title AS book_title, m.name AS member_name " +
            "FROM transactions t " +
            "JOIN books b ON t.book_id = b.book_id " +
            "JOIN members m ON t.member_id = m.member_id ";

    @Override
    public Transaction save(Transaction tx) throws Exceptions.DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            return save(conn, tx);
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error saving transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction save(Connection conn, Transaction tx) throws Exceptions.DatabaseException {
        String sql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, return_date, fine_amount, fine_paid, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, tx.getBookId());
            stmt.setInt(2, tx.getMemberId());
            stmt.setDate(3, Date.valueOf(tx.getIssueDate()));
            stmt.setDate(4, Date.valueOf(tx.getDueDate()));
            if (tx.getReturnDate() != null) stmt.setDate(5, Date.valueOf(tx.getReturnDate()));
            else stmt.setNull(5, Types.DATE);
            stmt.setDouble(6, tx.getFineAmount());
            stmt.setBoolean(7, tx.isFinePaid());
            stmt.setString(8, tx.getStatus().name());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) tx.setTransactionId(rs.getInt(1));
            }
            return tx;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Transaction SQL error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Transaction tx) throws Exceptions.DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            return update(conn, tx);
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Connection conn, Transaction tx) throws Exceptions.DatabaseException {
        String sql = "UPDATE transactions SET return_date=?, fine_amount=?, fine_paid=?, status=? WHERE transaction_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (tx.getReturnDate() != null) stmt.setDate(1, Date.valueOf(tx.getReturnDate()));
            else stmt.setNull(1, Types.DATE);
            stmt.setDouble(2, tx.getFineAmount());
            stmt.setBoolean(3, tx.isFinePaid());
            stmt.setString(4, tx.getStatus().name());
            stmt.setInt(5, tx.getTransactionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Update transaction error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(int id) throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findActiveTransactionByBookId(int bookId) throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "WHERE t.book_id = ? AND t.status = 'ISSUED' ORDER BY t.transaction_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findByMemberId(int memberId) throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "WHERE t.member_id = ? ORDER BY t.transaction_id DESC";
        return queryList(sql, memberId);
    }

    @Override
    public List<Transaction> findAll() throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "ORDER BY t.transaction_id DESC";
        return queryList(sql, null);
    }

    @Override
    public List<Transaction> findActiveTransactions() throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "WHERE t.status = 'ISSUED' ORDER BY t.due_date ASC";
        return queryList(sql, null);
    }

    @Override
    public List<Transaction> findOverdueTransactions() throws Exceptions.DatabaseException {
        String sql = SELECT_JOIN + "WHERE t.status = 'ISSUED' AND t.due_date < CURDATE() ORDER BY t.due_date ASC";
        return queryList(sql, null);
    }

    @Override
    public boolean markFineAsPaid(int id) throws Exceptions.DatabaseException {
        String sql = "UPDATE transactions SET fine_paid = TRUE WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    private List<Transaction> queryList(String sql, Integer param) throws Exceptions.DatabaseException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException(e.getMessage(), e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Date ret = rs.getDate("return_date");
        Transaction tx = new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                ret != null ? ret.toLocalDate() : null,
                rs.getDouble("fine_amount"),
                rs.getBoolean("fine_paid"),
                TransactionStatus.valueOf(rs.getString("status"))
        );
        tx.setBookTitle(rs.getString("book_title"));
        tx.setMemberName(rs.getString("member_name"));
        return tx;
    }
}
