package library;

/**
 * Base abstract class demonstrating:
 * - Abstraction
 * - Encapsulation
 * - Inheritance
 */
public abstract class Person {
    private int id;
    private String name;
    private String email;
    private String phone;

    public Person() {}

    public Person(int id, String name, String email, String phone) {
        this.id = id;
        setName(name);
        setEmail(email);
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("ID: %d | Name: %s | Email: %s | Phone: %s", id, name, email, phone);
    }
}
