import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class Main  {
    JFrame frame;
    JTextField titleField, authorField, isbnField;
    JTextArea displayArea;
    JRadioButton adminBtn, studentBtn;
    JButton addBookBtn, delBookBtn, searchBookBtn;

    String url = "jdbc:mysql://localhost:3306/library";
    String username = "root";
    String password = "myROOTpass";

    public Main() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 550);
        JPanel panel = new JPanel(new GridBagLayout());

        // I am using GridBagConstraints to arrange the components,
        // because this allows me to control the position and size of the components
        // that I am going to have

        GridBagConstraints gridBag = new GridBagConstraints();
        gridBag.insets = new Insets(10, 10, 10, 10);
        gridBag.anchor = GridBagConstraints.WEST;

        // Code to select the user type
        gridBag.gridx = 0;
        gridBag.gridy = 0;
        panel.add(new JLabel("User Type: "), gridBag);

        adminBtn = new JRadioButton("Admin");
        studentBtn = new JRadioButton("Student");

        ButtonGroup user = new ButtonGroup();
        user.add(adminBtn);
        user.add(studentBtn);

        gridBag.gridx = 1;
        gridBag.gridy = 0;
        panel.add(adminBtn, gridBag); // Added admin radio button

        gridBag.gridx = 2;
        gridBag.gridy = 0;
        panel.add(studentBtn, gridBag); // Added admin radio button
        // User typr selection part ended here

        adminBtn.addActionListener(e -> updateButtonVisibility());
        studentBtn.addActionListener(e -> updateButtonVisibility());

        // Area to type book name/title
        gridBag.gridx = 0;
        gridBag.gridy = 1;
        panel.add(new JLabel("Book title: "), gridBag);
        titleField = new JTextField(20);
        gridBag.gridx = 1;
        gridBag.gridy = 1;
        gridBag.gridwidth = 2;
        panel.add(titleField, gridBag);

        // Area to type author's name
        gridBag.gridx = 0;
        gridBag.gridy = 2;
        gridBag.gridwidth = 1;
        panel.add(new JLabel("Author name: "), gridBag);
        authorField = new JTextField(20);
        gridBag.gridx = 1;
        gridBag.gridy = 2;
        gridBag.gridwidth = 2;
        panel.add(authorField, gridBag);

        // Area to enter ISBN
        gridBag.gridx = 0;
        gridBag.gridy = 3;
        gridBag.gridwidth = 1;
        panel.add(new JLabel("Enter ISBN: "), gridBag);
        isbnField = new JTextField(20);
        gridBag.gridx = 1;
        gridBag.gridy = 3;
        gridBag.gridwidth = 2;
        panel.add(isbnField, gridBag);

        // "Add book", "Delete book" and "Search book" buttons
        addBookBtn = new JButton("Add book");
        delBookBtn = new JButton("Delete book");
        searchBookBtn = new JButton("Search book");

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonsPanel.add(addBookBtn);
        buttonsPanel.add(delBookBtn);
        buttonsPanel.add(searchBookBtn);

        gridBag.gridx = 1;
        gridBag.gridy = 4;
        gridBag.gridwidth = 2;
        panel.add(buttonsPanel, gridBag);

        // Area to display the books (added, deleted, found bookk)
        gridBag.gridx = 0;
        gridBag.gridy = 5;
        panel.add(new JLabel("Books status: "), gridBag);
        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        gridBag.gridx = 0;
        gridBag.gridy = 6;
        gridBag.gridwidth = 3;
        gridBag.fill = GridBagConstraints.BOTH; // this line will help adjust the scroll pane size to teh
        // size of the text (if the text is more lengthy than the defaultly set size
        panel.add(scrollPane, gridBag);

        frame.add(panel);
        frame.setVisible(true);
        // Now all the above components are added to the frrame

        addBookBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(adminBtn.isSelected()) {
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();
                    String isbn = isbnField.getText().trim();

                    if(title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                        JOptionPane.showMessageDialog(frame,"All fields must be filled", "Input error!", JOptionPane.ERROR_MESSAGE);
                    } else {
                        saveBookToDB(title, author, isbn);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Only admins can add books.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }

                titleField.setText("");
                authorField.setText("");
                isbnField.setText("");
            }
        });

        delBookBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (adminBtn.isSelected()) {
                    String isbn = isbnField.getText().trim();

                    if(isbn.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "ISBN must be filled out to delete a book.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        delBookFromDB(isbn);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Only admins can delete books.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }

                titleField.setText("");
                authorField.setText("");
                isbnField.setText("");
            }
        });

        searchBookBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookToSearch = titleField.getText().trim();

                if(bookToSearch.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Search keyword must be filled out.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    searchBooks(bookToSearch);
                }
            }
        });



    }

    private void updateButtonVisibility() {
        addBookBtn.setVisible(adminBtn.isSelected());
        delBookBtn.setVisible(adminBtn.isSelected());
        searchBookBtn.setVisible(true);
    }

    private void searchBooks(String bookToSearch) {
        String sql = "SELECT * FROM books WHERE title LIKE ? OR auhtor LIKE ? OR isbn LIKE ?";

        List<Book> books = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, username, password);
        PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchKeyword = "%" + bookToSearch + "%";
            ps.setString(1, searchKeyword);
            ps.setString(2, searchKeyword);
            ps.setString(3, searchKeyword);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                Book book = new Book(title, author, isbn);
                books.add(book);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error searching books: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        displayArea.setText("");

        if(books.isEmpty()) {
            displayArea.append("No books found.\n");
        } else {
            for (Book book : books) {
                displayArea.append(book.toString() + "\n");
            }
        }
    }

    private void delBookFromDB(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
        PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                displayArea.append("Book deleted with ISBN: " + isbn + "\n");
            } else {
                displayArea.append("No book found with ISBN: " + isbn + "\n");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting book: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveBookToDB(String title, String author, String isbn) {
        String sql = "INSERT into books (title, author, isbn) VALUES (?, ?, ?)";

        try(Connection conn = DriverManager.getConnection(url, username, password);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);

            ps.executeUpdate();

            displayArea.append("Book added: " + title + " by " + author + " (ISBN: " +  isbn +")\n");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error saving book: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found");
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }

    record Book(String title, String author, String isbn) {
        @Override
        public String toString() {
            return "Title: " + title + ", Author: " + author + ", ISBN: " + isbn;
        }
    }
}
