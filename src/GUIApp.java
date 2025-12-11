import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// ===================== ENTRY POINT FOR GUI =====================
public class GUIApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}

// ===================== LOGIN WINDOW =====================
class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final AdminDAO adminDao = new AdminDAO();

    public LoginFrame() {
        setTitle("Student Management System - Login");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> doLogin());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (adminDao.authenticate(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful. Welcome, " + username + "!");
            this.dispose();
            new MainMenuFrame(username).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ===================== MAIN MENU WINDOW =====================
class MainMenuFrame extends JFrame {
    private final StudentDAO studentDao = new StudentDAO();
    private final String currentUser;

    public MainMenuFrame(String username) {
        this.currentUser = username;

        setTitle("Student Management System - Logged in as " + username);
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton addStudentBtn = new JButton("Add New Student");
        JButton viewStudentsBtn = new JButton("View All Students");
        JButton searchRollBtn = new JButton("Search by Roll No");
        JButton searchNameBtn = new JButton("Search by Name");
        JButton filterYearBtn = new JButton("Filter by Year");
        JButton filterCourseBtn = new JButton("Filter by Course");
        JButton addAdminBtn = new JButton("Add New Admin");
        JButton deleteStudentBtn = new JButton("Delete Student"); // NEW
        JButton logoutBtn = new JButton("Logout");
        JButton exitBtn = new JButton("Exit");

        panel.add(addStudentBtn);
        panel.add(viewStudentsBtn);
        panel.add(searchRollBtn);
        panel.add(searchNameBtn);
        panel.add(filterYearBtn);
        panel.add(filterCourseBtn);
        panel.add(addAdminBtn);
        panel.add(deleteStudentBtn); // added to GUI
        panel.add(logoutBtn);
        panel.add(exitBtn);

        add(panel);

        addStudentBtn.addActionListener(e -> new AddStudentFrame(studentDao).setVisible(true));
        viewStudentsBtn.addActionListener(e -> openStudentsTable(studentDao.getAllStudents(), "All Students"));
        searchRollBtn.addActionListener(e -> searchByRoll());
        searchNameBtn.addActionListener(e -> searchByName());
        filterYearBtn.addActionListener(e -> filterByYear());
        filterCourseBtn.addActionListener(e -> filterByCourse());
        addAdminBtn.addActionListener(e -> new AddAdminFrame().setVisible(true));
        deleteStudentBtn.addActionListener(e -> deleteStudent()); // NEW listener
        logoutBtn.addActionListener(e -> logout());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private void openStudentsTable(List<Student> students, String title) {
        new ViewStudentsFrame(students, title).setVisible(true);
    }

    private void searchByRoll() {
        String roll = JOptionPane.showInputDialog(this, "Enter Roll No:");
        if (roll == null || roll.trim().isEmpty()) return;

        Student s = studentDao.getStudentByRollNo(roll.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "No student found with roll no: " + roll);
        } else {
            new ViewStudentsFrame(List.of(s), "Student with Roll No: " + roll).setVisible(true);
        }
    }

    private void searchByName() {
        String keyword = JOptionPane.showInputDialog(this, "Enter name or part of name:");
        if (keyword == null || keyword.trim().isEmpty()) return;

        List<Student> students = studentDao.searchByName(keyword.trim());
        openStudentsTable(students, "Search Results for: " + keyword);
    }

    private void filterByYear() {
        String yearStr = JOptionPane.showInputDialog(this, "Enter Year (1-4):");
        if (yearStr == null || yearStr.trim().isEmpty()) return;
        try {
            int year = Integer.parseInt(yearStr.trim());
            List<Student> students = studentDao.filterByYear(year);
            openStudentsTable(students, "Students in Year: " + year);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year entered.");
        }
    }

    private void filterByCourse() {
        String course = JOptionPane.showInputDialog(this, "Enter course (e.g., B.Tech):");
        if (course == null || course.trim().isEmpty()) return;
        List<Student> students = studentDao.filterByCourse(course.trim());
        openStudentsTable(students, "Students in Course: " + course);
    }

    private void deleteStudent() {
        String roll = JOptionPane.showInputDialog(this, "Enter Roll No to Delete:");
        if (roll == null || roll.trim().isEmpty()) return;

        Student s = studentDao.getStudentByRollNo(roll.trim());
        if (s == null) {
            JOptionPane.showMessageDialog(this, "No student found with roll no: " + roll);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete student: " + s.getName() + " (Roll: " + s.getRollNo() + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = studentDao.deleteStudent(roll.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete student.");
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Logout and return to login screen?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}

// ===================== ADD STUDENT WINDOW =====================
class AddStudentFrame extends JFrame {
    private final JTextField rollField, nameField, courseField, yearField, marksField;
    private final StudentDAO dao;

    public AddStudentFrame(StudentDAO dao) {
        this.dao = dao;

        setTitle("Add New Student");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 5, 5));
        setResizable(false);

        add(new JLabel("Roll No:"));
        rollField = new JTextField();
        add(rollField);

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Course:"));
        courseField = new JTextField();
        add(courseField);

        add(new JLabel("Year (1-4):"));
        yearField = new JTextField();
        add(yearField);

        add(new JLabel("Marks (out of 1000):"));
        marksField = new JTextField();
        add(marksField);

        JButton saveBtn = new JButton("Save");
        JButton closeBtn = new JButton("Close");
        add(saveBtn);
        add(closeBtn);

        saveBtn.addActionListener(e -> saveStudent());
        closeBtn.addActionListener(e -> dispose());
    }

    private void saveStudent() {
        try {
            String roll = rollField.getText().trim();
            String name = nameField.getText().trim();
            String course = courseField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            int marks = Integer.parseInt(marksField.getText().trim());

            if (roll.isEmpty() || name.isEmpty() || course.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            Student s = new Student(roll, name, course, year, marks, null, null);
            boolean ok = dao.addStudent(s);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Student added successfully!");
                rollField.setText("");
                nameField.setText("");
                courseField.setText("");
                yearField.setText("");
                marksField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add student.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year and Marks must be valid integers.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}

// ===================== VIEW STUDENTS TABLE WINDOW =====================
class ViewStudentsFrame extends JFrame {
    public ViewStudentsFrame(List<Student> students, String title) {
        setTitle(title);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"ID", "Roll No", "Name", "Course", "Year", "Marks", "Grade", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        if (students != null) {
            for (Student s : students) {
                Object[] row = {
                        s.getId(),
                        s.getRollNo(),
                        s.getName(),
                        s.getCourse(),
                        s.getYear(),
                        s.getMarks(),
                        s.getGrade(),
                        s.getStatus()
                };
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
}

// ===================== ADD ADMIN WINDOW =====================
class AddAdminFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final AdminDAO adminDao = new AdminDAO();

    public AddAdminFrame() {
        setTitle("Add New Admin");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 5, 5));
        setResizable(false);

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton saveBtn = new JButton("Save");
        JButton closeBtn = new JButton("Close");
        add(saveBtn);
        add(closeBtn);

        saveBtn.addActionListener(e -> saveAdmin());
        closeBtn.addActionListener(e -> dispose());
    }

    private void saveAdmin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required.");
            return;
        }

        boolean ok = adminDao.addAdmin(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this, "New admin added successfully!");
            usernameField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add admin.");
        }
    }
}