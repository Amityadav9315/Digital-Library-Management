import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibraryManagement {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "library_user"; // Use the new user
    private static final String PASSWORD = "yourpassword"; // Use the new password

    // Method to get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Digital Library Management System");
            System.out.println("1. Register User");
            System.out.println("2. Add Book");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. Display Books");
            System.out.println("6. Display Users");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter User ID: ");
                        String userId = scanner.next();
                        System.out.print("Enter User Name: ");
                        String userName = scanner.next();
                        User user = new User(userId, userName);
                        library.registerUser(user);
                        break;

                    case 2:
                        System.out.print("Enter Book ID: ");
                        String bookId = scanner.next();
                        System.out.print("Enter Book Title: ");
                        String title = scanner.next();
                        System.out.print("Enter Book Author: ");
                        String author = scanner.next();
                        Book book = new Book(bookId, title, author);
                        library.addBook(book);
                        break;

                    case 3:
                        System.out.print("Enter User ID: ");
                        String borrowUserId = scanner.next();
                        System.out.print("Enter Book ID: ");
                        String borrowBookId = scanner.next();
                        if (library.borrowBook(borrowUserId, borrowBookId)) {
                            System.out.println("Book borrowed successfully.");
                        } else {
                            System.out.println("Failed to borrow book.");
                        }
                        break;

                    case 4:
                        System.out.print("Enter User ID: ");
                        String returnUserId = scanner.next();
                        System.out.print("Enter Book ID: ");
                        String returnBookId = scanner.next();
                        if (library.returnBook(returnUserId, returnBookId)) {
                            System.out.println("Book returned successfully.");
                        } else {
                            System.out.println("Failed to return book.");
                        }
                        break;

                    case 5:
                        library.displayBooks();
                        break;

                    case 6:
                        library.displayUsers();
                        break;

                    case 7:
                        System.out.println("Exiting...");
                        scanner.close();
                        System.exit(0);

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }
}

class Book {
    private String id;
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true; // Default availability is true
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Method to save a book to the database
    public void save() throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "INSERT INTO books (id, title, author, isAvailable) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, title);
            stmt.setString(3, author);
            stmt.setBoolean(4, isAvailable);
            stmt.executeUpdate();
        }
    }

    // Method to find a book by its ID
    public static Book findById(String bookId) throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "SELECT * FROM books WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Book book = new Book(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                book.setAvailable(rs.getBoolean("isAvailable"));
                return book;
            }
        }
        return null;
    }

    // Method to update book availability in the database
    public void update() throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "UPDATE books SET isAvailable = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, isAvailable);
            stmt.setString(2, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}

class User {
    private String userId;
    private String name;
    private List<Book> borrowedBooks;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    // Method to save a user to the database
    public void save() throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "INSERT INTO users (userId, name) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    // Method to find a user by their ID
    public static User findById(String userId) throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "SELECT * FROM users WHERE userId = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getString("userId"),
                        rs.getString("name")
                );
                return user;
            }
        }
        return null;
    }

    // Method to borrow a book
    public void borrowBook(Book book) throws SQLException {
        borrowedBooks.add(book);
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "INSERT INTO borrowed_books (userId, bookId) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, book.getId());
            stmt.executeUpdate();
        }
    }

    // Method to return a book
    public void returnBook(Book book) throws SQLException {
        borrowedBooks.remove(book);
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "DELETE FROM borrowed_books WHERE userId = ? AND bookId = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, book.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", borrowedBooks=" + borrowedBooks +
                '}';
    }
}

class Library {
    private List<Book> books;
    private List<User> users;

    public Library() {
        books = new ArrayList<>();
        users = new ArrayList<>();
    }

    // Method to add a book to the library
    public void addBook(Book book) throws SQLException {
        books.add(book);
        book.save();
    }

    // Method to register a user to the library
    public void registerUser(User user) throws SQLException {
        users.add(user);
        user.save();
    }

    // Method to find a book by its ID
    public Book findBookById(String bookId) throws SQLException {
        return Book.findById(bookId);
    }

    // Method to find a user by their ID
    public User findUserById(String userId) throws SQLException {
        return User.findById(userId);
    }

    // Method to borrow a book
    public boolean borrowBook(String userId, String bookId) throws SQLException {
        User user = findUserById(userId);
        Book book = findBookById(bookId);

        if (user != null && book != null && book.isAvailable()) {
            book.setAvailable(false);
            book.update();
            user.borrowBook(book);
            return true;
        }
        return false;
    }

    // Method to return a borrowed book
    public boolean returnBook(String userId, String bookId) throws SQLException {
        User user = findUserById(userId);
        Book book = findBookById(bookId);

        if (user != null && book != null && !book.isAvailable()) {
            book.setAvailable(true);
            book.update();
            user.returnBook(book);
            return true;
        }
        return false;
    }

    // Method to display all books in the library
    public void displayBooks() throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "SELECT * FROM books";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                book.setAvailable(rs.getBoolean("isAvailable"));
                System.out.println(book);
            }
        }
    }

    // Method to display all users in the library
    public void displayUsers() throws SQLException {
        try (Connection conn = LibraryManagement.getConnection()) {
            String sql = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getString("userId"),
                        rs.getString("name")
                );
                System.out.println(user);
            }
        }
    }
}
