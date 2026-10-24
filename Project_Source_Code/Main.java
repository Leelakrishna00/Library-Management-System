package library;

import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LibraryServiceImpl service = new LibraryServiceImpl();

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("                   LIBRARY MANAGEMENT SYSTEM                        ");
        System.out.println("      Tech Stack: Core Java + JDBC + MySQL + OOP Architecture       ");
        System.out.println("      Storage: " + (DAOFactory.isUsingInMemory() ? "In-Memory Simulation (Sample Data)" : "Live MySQL via JDBC"));
        System.out.println("====================================================================");

        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. View All Books");
            System.out.println("2. Add New Book");
            System.out.println("3. Search Books");
            System.out.println("4. View All Members");
            System.out.println("5. Register Member");
            System.out.println("6. Issue Book (Checkout)");
            System.out.println("7. Return Book (Checkin & Fine Calculation)");
            System.out.println("8. View Active Issued Loans");
            System.out.println("9. View Overdue Loans & Pending Fines");
            System.out.println("10. Pay / Settle Fine");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                System.out.println("Thank you for using Library Management System!");
                break;
            }

            try {
                switch (input) {
                    case "1":
                        System.out.println("\n--- ALL BOOKS ---");
                        for (Book b : service.getAllBooks()) System.out.println(b);
                        break;
                    case "2":
                        System.out.print("Enter ISBN: "); String isbn = scanner.nextLine().trim();
                        System.out.print("Enter Title: "); String title = scanner.nextLine().trim();
                        System.out.print("Enter Author: "); String author = scanner.nextLine().trim();
                        System.out.print("Enter Category: "); String cat = scanner.nextLine().trim();
                        System.out.print("Enter Published Year: "); int yr = Integer.parseInt(scanner.nextLine().trim());
                        Book saved = service.registerBook(new Book(0, isbn, title, author, cat, yr, true));
                        System.out.println("✔ Book added successfully with ID #" + saved.getBookId());
                        break;
                    case "3":
                        System.out.print("Enter keyword to search: "); String q = scanner.nextLine().trim();
                        List<Book> matches = service.searchBooks(q);
                        System.out.println("Found " + matches.size() + " matches:");
                        for (Book b : matches) System.out.println(b);
                        break;
                    case "4":
                        System.out.println("\n--- ALL MEMBERS ---");
                        for (Member m : service.getAllMembers()) System.out.println(m);
                        break;
                    case "5":
                        System.out.print("Enter Name: "); String name = scanner.nextLine().trim();
                        System.out.print("Enter Email: "); String email = scanner.nextLine().trim();
                        System.out.print("Enter Phone: "); String phone = scanner.nextLine().trim();
                        Member m = service.registerMember(new Member(0, name, email, phone, null, 3, "ACTIVE"));
                        System.out.println("✔ Member registered successfully with ID #" + m.getId());
                        break;
                    case "6":
                        System.out.print("Enter Book ID to issue: "); int bId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Member ID: "); int mId = Integer.parseInt(scanner.nextLine().trim());
                        Transaction tx = service.issueBook(bId, mId);
                        System.out.println("✔ Book issued successfully! Due Date: " + tx.getDueDate());
                        break;
                    case "7":
                        System.out.print("Enter Book ID being returned: "); int retId = Integer.parseInt(scanner.nextLine().trim());
                        Transaction retTx = service.returnBook(retId);
                        System.out.println("✔ Book returned successfully!");
                        if (retTx.getFineAmount() > 0) {
                            System.out.printf("⚠ OVERDUE! Fine calculated: ₹%.2f (@ ₹%.2f/day)\n", retTx.getFineAmount(), DBConnection.getFinePerDay());
                        } else {
                            System.out.println("✔ Returned on time. No overdue fine.");
                        }
                        break;
                    case "8":
                        System.out.println("\n--- ACTIVE ISSUED LOANS ---");
                        for (Transaction t : service.getActiveLoans()) System.out.println(t);
                        break;
                    case "9":
                        System.out.println("\n--- OVERDUE LOANS ---");
                        for (Transaction t : service.getOverdueLoans()) System.out.println(t);
                        break;
                    case "10":
                        System.out.print("Enter Transaction ID to settle fine: "); int txId = Integer.parseInt(scanner.nextLine().trim());
                        service.payFine(txId);
                        System.out.println("✔ Fine marked as paid.");
                        break;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            } catch (Exception e) {
                System.out.println("✖ ERROR: " + e.getMessage());
            }
        }
    }
}
