package library;

/**
 * Custom Exception Hierarchy for the Library Management System.
 */
public class Exceptions {
    public static class LibraryException extends Exception {
        public LibraryException(String message) { super(message); }
        public LibraryException(String message, Throwable cause) { super(message, cause); }
    }

    public static class BookNotFoundException extends LibraryException {
        public BookNotFoundException(String message) { super(message); }
    }

    public static class BookNotAvailableException extends LibraryException {
        public BookNotAvailableException(String message) { super(message); }
    }

    public static class MemberNotFoundException extends LibraryException {
        public MemberNotFoundException(String message) { super(message); }
    }

    public static class MaxBorrowLimitExceededException extends LibraryException {
        public MaxBorrowLimitExceededException(String message) { super(message); }
    }

    public static class DatabaseException extends LibraryException {
        public DatabaseException(String message) { super(message); }
        public DatabaseException(String message, Throwable cause) { super(message, cause); }
    }
}
