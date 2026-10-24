package library;

import java.time.LocalDate;

public class Member extends Person {
    private LocalDate membershipDate;
    private int maxBorrowLimit;
    private String status;

    public Member() {
        super();
        this.membershipDate = LocalDate.now();
        this.maxBorrowLimit = 3;
        this.status = "ACTIVE";
    }

    public Member(int id, String name, String email, String phone, LocalDate membershipDate, int maxBorrowLimit, String status) {
        super(id, name, email, phone);
        this.membershipDate = membershipDate != null ? membershipDate : LocalDate.now();
        this.maxBorrowLimit = maxBorrowLimit > 0 ? maxBorrowLimit : 3;
        this.status = status != null ? status : "ACTIVE";
    }

    public LocalDate getMembershipDate() { return membershipDate; }
    public void setMembershipDate(LocalDate membershipDate) { this.membershipDate = membershipDate; }

    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public void setMaxBorrowLimit(int maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(this.status); }

    @Override
    public String getRole() { return "Member"; }

    @Override
    public String toString() {
        return String.format("%s | Role: %s | Limit: %d | Status: %s", super.toString(), getRole(), maxBorrowLimit, status);
    }
}
