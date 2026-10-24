package library;

import java.time.LocalDate;

public class Transaction {
    private int transactionId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private boolean finePaid;
    private TransactionStatus status;

    private String bookTitle;
    private String memberName;

    public Transaction() {
        this.status = TransactionStatus.ISSUED;
        this.fineAmount = 0.0;
        this.finePaid = false;
    }

    public Transaction(int transactionId, int bookId, int memberId, LocalDate issueDate,
                       LocalDate dueDate, LocalDate returnDate, double fineAmount,
                       boolean finePaid, TransactionStatus status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.finePaid = finePaid;
        this.status = status;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = Math.max(0.0, fineAmount); }

    public boolean isFinePaid() { return finePaid; }
    public void setFinePaid(boolean finePaid) { this.finePaid = finePaid; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    @Override
    public String toString() {
        String bookInfo = (bookTitle != null) ? bookTitle : ("Book #" + bookId);
        String memberInfo = (memberName != null) ? memberName : ("Member #" + memberId);
        return String.format("Txn #%d | %s -> %s | Issued: %s | Due: %s | Returned: %s | Fine: ₹%.2f | Status: %s",
                transactionId, bookInfo, memberInfo, issueDate, dueDate,
                (returnDate != null ? returnDate.toString() : "PENDING"),
                fineAmount, status);
    }
}
