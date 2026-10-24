package library;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface MemberDAO {
    Member save(Member member) throws Exceptions.DatabaseException;
    boolean update(Member member) throws Exceptions.DatabaseException;
    boolean delete(int memberId) throws Exceptions.DatabaseException;
    Optional<Member> findById(int memberId) throws Exceptions.DatabaseException;
    Optional<Member> findByEmail(String email) throws Exceptions.DatabaseException;
    List<Member> findAll() throws Exceptions.DatabaseException;
    List<Member> search(String query) throws Exceptions.DatabaseException;
    int getActiveBorrowCount(int memberId) throws Exceptions.DatabaseException;
}
