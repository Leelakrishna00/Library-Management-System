package library;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class MemberDAOImpl implements MemberDAO {
    @Override
    public Member save(Member member) throws Exceptions.DatabaseException {
        String sql = "INSERT INTO members (name, email, phone, membership_date, max_borrow_limit, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setDate(4, Date.valueOf(member.getMembershipDate()));
            stmt.setInt(5, member.getMaxBorrowLimit());
            stmt.setString(6, member.getStatus());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) member.setId(rs.getInt(1));
            }
            return member;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error saving member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Member member) throws Exceptions.DatabaseException {
        String sql = "UPDATE members SET name=?, email=?, phone=?, max_borrow_limit=?, status=? WHERE member_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setInt(4, member.getMaxBorrowLimit());
            stmt.setString(5, member.getStatus());
            stmt.setInt(6, member.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error updating member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int memberId) throws Exceptions.DatabaseException {
        String sql = "DELETE FROM members WHERE member_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error deleting member: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Member> findById(int memberId) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM members WHERE member_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error finding member: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM members WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error finding member by email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Member> findAll() throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM members ORDER BY name ASC";
        List<Member> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error retrieving members: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Member> search(String query) throws Exceptions.DatabaseException {
        String sql = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ? OR phone LIKE ?";
        List<Member> list = new ArrayList<>();
        String q = "%" + query + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q);
            stmt.setString(2, q);
            stmt.setString(3, q);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error searching members: " + e.getMessage(), e);
        }
    }

    @Override
    public int getActiveBorrowCount(int memberId) throws Exceptions.DatabaseException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE member_id=? AND status='ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new Exceptions.DatabaseException("Error counting active borrows: " + e.getMessage(), e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Date d = rs.getDate("membership_date");
        return new Member(
                rs.getInt("member_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                d != null ? d.toLocalDate() : null,
                rs.getInt("max_borrow_limit"),
                rs.getString("status")
        );
    }
}
