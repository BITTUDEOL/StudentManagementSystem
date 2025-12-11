import java.sql.*;
import java.util.*;

// ===================== MODEL CLASS =====================
class Student {
    private int id;
    private String rollNo;
    private String name;
    private String course;
    private int year;
    private int marks;
    private String grade;
    private String status;

    public Student(int id, String rollNo, String name, String course,
                   int year, int marks, String grade, String status) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.course = course;
        this.year = year;
        this.marks = marks;
        this.grade = grade;
        this.status = status;
    }

    public Student(String rollNo, String name, String course,
                   int year, int marks, String grade, String status) {
        this(0, rollNo, name, course, year, marks, grade, status);
    }

    public int getId() {
        return id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public int getYear() {
        return year;
    }

    public int getMarks() {
        return marks;
    }

    public String getGrade() {
        return grade;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format(
                "%-5d %-10s %-20s %-10s %-5d %-7d %-5s %-8s",
                id, rollNo, name, course, year, marks, grade, status
        );
    }
}

// ===================== DB CONNECTION =====================
class DBConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/studentdb?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";                // change if needed
    private static final String PASS = "Jaisimha@321"; // TODO: put your password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Make sure the connector JAR is added.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

// ===================== ADMIN DAO (LOGIN + MANAGE) =====================
class AdminDAO {

    public boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();  // true if a row exists
            }
        } catch (SQLException e) {
            System.out.println("Error while authenticating admin: " + e.getMessage());
        }
        return false;
    }

    public boolean addAdmin(String username, String password) {
        String sql = "INSERT INTO admins (username, password) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Username already exists. Please choose a different username.");
        } catch (SQLException e) {
            System.out.println("Error while adding admin: " + e.getMessage());
        }
        return false;
    }
}

// ===================== STUDENT DAO =====================
class StudentDAO {

    // Helper: calculate grade + status (assuming marks out of 1000)
    private String[] calculateGradeAndStatus(int marks) {
        int percentage = (marks * 100) / 1000; // e.g., 900 -> 90%
        String grade;
        String status;

        if (percentage >= 90) {
            grade = "A";
            status = "Pass";
        } else if (percentage >= 75) {
            grade = "B";
            status = "Pass";
        } else if (percentage >= 50) {
            grade = "C";
            status = "Pass";
        } else {
            grade = "F";
            status = "Fail";
        }
        return new String[]{grade, status};
    }

    // Create / Insert
    public boolean addStudent(Student student) {
        String[] gs = calculateGradeAndStatus(student.getMarks());
        String grade = gs[0];
        String status = gs[1];

        String sql = "INSERT INTO students (roll_no, name, course, year, marks, grade, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getRollNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getCourse());
            ps.setInt(4, student.getYear());
            ps.setInt(5, student.getMarks());
            ps.setString(6, grade);
            ps.setString(7, status);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Roll number must be unique. A student with this roll no already exists.");
        } catch (SQLException e) {
            System.out.println("Error while adding student: " + e.getMessage());
        }
        return false;
    }

    // Read all
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("id"),
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getInt("year"),
                        rs.getInt("marks"),
                        rs.getString("grade"),
                        rs.getString("status")
                );
                list.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching students: " + e.getMessage());
        }

        return list;
    }

    // Read by roll number
    public Student getStudentByRollNo(String rollNo) {
        String sql = "SELECT * FROM students WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course"),
                            rs.getInt("year"),
                            rs.getInt("marks"),
                            rs.getString("grade"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching student: " + e.getMessage());
        }
        return null;
    }

    // Search by name (LIKE)
    public List<Student> searchByName(String keyword) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE name LIKE ? ORDER BY name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course"),
                            rs.getInt("year"),
                            rs.getInt("marks"),
                            rs.getString("grade"),
                            rs.getString("status")
                    );
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while searching students by name: " + e.getMessage());
        }

        return list;
    }

    // Filter by year
    public List<Student> filterByYear(int year) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE year = ? ORDER BY name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course"),
                            rs.getInt("year"),
                            rs.getInt("marks"),
                            rs.getString("grade"),
                            rs.getString("status")
                    );
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while filtering students by year: " + e.getMessage());
        }

        return list;
    }

    // Filter by course
    public List<Student> filterByCourse(String course) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE course LIKE ? ORDER BY year, name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + course + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course"),
                            rs.getInt("year"),
                            rs.getInt("marks"),
                            rs.getString("grade"),
                            rs.getString("status")
                    );
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while filtering students by course: " + e.getMessage());
        }

        return list;
    }

    // Update
    public boolean updateStudent(Student student) {
        String[] gs = calculateGradeAndStatus(student.getMarks());
        String grade = gs[0];
        String status = gs[1];

        String sql = "UPDATE students " +
                "SET name = ?, course = ?, year = ?, marks = ?, grade = ?, status = ? " +
                "WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getCourse());
            ps.setInt(3, student.getYear());
            ps.setInt(4, student.getMarks());
            ps.setString(5, grade);
            ps.setString(6, status);
            ps.setString(7, student.getRollNo());

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error while updating student: " + e.getMessage());
        }
        return false;
    }

    // Delete
    public boolean deleteStudent(String rollNo) {
        String sql = "DELETE FROM students WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Error while deleting student: " + e.getMessage());
        }
        return false;
    }
}

// ===================== MAIN / CONSOLE UI =====================
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO dao = new StudentDAO();
    private static final AdminDAO adminDao = new AdminDAO();

    public static void main(String[] args) {
        if (!loginMenu()) {
            System.out.println("Too many failed attempts. Exiting...");
            return;
        }

        while (true) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    addStudentMenu();
                    break;
                case 2:
                    viewAllStudentsMenu();
                    break;
                case 3:
                    searchStudentByRollMenu();
                    break;
                case 4:
                    updateStudentMenu();
                    break;
                case 5:
                    deleteStudentMenu();
                    break;
                case 6:
                    searchByNameMenu();
                    break;
                case 7:
                    filterByYearMenu();
                    break;
                case 8:
                    filterByCourseMenu();
                    break;
                case 9:
                    addAdminMenu();
                    break;
                case 10:
                    System.out.println("Exiting... Bye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please choose between 1-10.");
            }
        }
    }

    // ------------- LOGIN MENU -------------
    private static boolean loginMenu() {
        System.out.println("===== STUDENT MANAGEMENT SYSTEM LOGIN =====");
        int attempts = 0;

        while (attempts < 3) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (adminDao.authenticate(username, password)) {
                System.out.println("Login successful. Welcome, " + username + "!");
                return true;
            } else {
                attempts++;
                System.out.println("Invalid credentials. Attempts left: " + (3 - attempts));
            }
        }
        return false;
    }

    // ------------- MAIN MENU -------------
    private static void printMenu() {
        System.out.println("\n==== STUDENT RECORD MANAGEMENT SYSTEM ====");
        System.out.println("1. Add New Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student by Roll No");
        System.out.println("4. Update Student Details");
        System.out.println("5. Delete Student");
        System.out.println("6. Search Students by Name");
        System.out.println("7. Filter Students by Year");
        System.out.println("8. Filter Students by Course");
        System.out.println("9. Add New Admin");
        System.out.println("10. Exit");
        System.out.println("==========================================");
    }

    // ------------- MENUS -------------
    private static void addStudentMenu() {
        System.out.println("\n--- Add New Student ---");
        System.out.print("Enter Roll No: ");
        String rollNo = scanner.nextLine().trim();

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Course (e.g., B.Tech): ");
        String course = scanner.nextLine().trim();

        int year = readInt("Enter Year of Study (1-4): ");
        int marks = readInt("Enter Total Marks (out of 1000): ");

        Student student = new Student(rollNo, name, course, year, marks, null, null);
        boolean success = dao.addStudent(student);
        if (success) {
            System.out.println("Student added successfully.");
        } else {
            System.out.println("Failed to add student.");
        }
    }

    private static void viewAllStudentsMenu() {
        System.out.println("\n--- All Students ---");
        List<Student> students = dao.getAllStudents();
        printStudentsList(students);
    }

    private static void searchStudentByRollMenu() {
        System.out.println("\n--- Search Student By Roll No ---");
        System.out.print("Enter Roll No: ");
        String rollNo = scanner.nextLine().trim();

        Student s = dao.getStudentByRollNo(rollNo);
        if (s == null) {
            System.out.println("No student found with roll no: " + rollNo);
        } else {
            printStudentsHeader();
            System.out.println(s);
        }
    }

    private static void searchByNameMenu() {
        System.out.println("\n--- Search Students By Name ---");
        System.out.print("Enter name or part of name: ");
        String keyword = scanner.nextLine().trim();

        List<Student> students = dao.searchByName(keyword);
        printStudentsList(students);
    }

    private static void filterByYearMenu() {
        System.out.println("\n--- Filter Students By Year ---");
        int year = readInt("Enter Year (1-4): ");

        List<Student> students = dao.filterByYear(year);
        printStudentsList(students);
    }

    private static void filterByCourseMenu() {
        System.out.println("\n--- Filter Students By Course ---");
        System.out.print("Enter course (e.g., B.Tech): ");
        String course = scanner.nextLine().trim();

        List<Student> students = dao.filterByCourse(course);
        printStudentsList(students);
    }

    private static void addAdminMenu() {
        System.out.println("\n--- Add New Admin ---");
        System.out.print("Enter new admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter new admin password: ");
        String password = scanner.nextLine().trim();

        boolean success = adminDao.addAdmin(username, password);
        if (success) {
            System.out.println("New admin added successfully.");
        } else {
            System.out.println("Failed to add admin.");
        }
    }

    private static void updateStudentMenu() {
        System.out.println("\n--- Update Student ---");
        System.out.print("Enter Roll No of student to update: ");
        String rollNo = scanner.nextLine().trim();

        Student existing = dao.getStudentByRollNo(rollNo);
        if (existing == null) {
            System.out.println("No student found with roll no: " + rollNo);
            return;
        }

        System.out.println("Current details:");
        printStudentsHeader();
        System.out.println(existing);

        System.out.println("\nEnter new details (press Enter to keep current value):");

        System.out.print("Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            existing.setName(name);
        }

        System.out.print("Course [" + existing.getCourse() + "]: ");
        String course = scanner.nextLine().trim();
        if (!course.isEmpty()) {
            existing.setCourse(course);
        }

        System.out.print("Year [" + existing.getYear() + "]: ");
        String yearInput = scanner.nextLine().trim();
        if (!yearInput.isEmpty()) {
            try {
                int year = Integer.parseInt(yearInput);
                existing.setYear(year);
            } catch (NumberFormatException e) {
                System.out.println("Invalid year entered. Keeping old value.");
            }
        }

        System.out.print("Marks [" + existing.getMarks() + "]: ");
        String marksInput = scanner.nextLine().trim();
        if (!marksInput.isEmpty()) {
            try {
                int marks = Integer.parseInt(marksInput);
                existing.setMarks(marks);
            } catch (NumberFormatException e) {
                System.out.println("Invalid marks entered. Keeping old value.");
            }
        }

        boolean success = dao.updateStudent(existing);
        if (success) {
            System.out.println("Student updated successfully.");
        } else {
            System.out.println("Failed to update student.");
        }
    }

    private static void deleteStudentMenu() {
        System.out.println("\n--- Delete Student ---");
        System.out.print("Enter Roll No of student to delete: ");
        String rollNo = scanner.nextLine().trim();

        Student existing = dao.getStudentByRollNo(rollNo);
        if (existing == null) {
            System.out.println("No student found with roll no: " + rollNo);
            return;
        }

        System.out.println("Are you sure you want to delete this record? (y/n)");
        printStudentsHeader();
        System.out.println(existing);
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            boolean success = dao.deleteStudent(rollNo);
            if (success) {
                System.out.println("Student deleted successfully.");
            } else {
                System.out.println("Failed to delete student.");
            }
        } else {
            System.out.println("Delete operation cancelled.");
        }
    }

    // ------------- HELPERS -------------
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static void printStudentsHeader() {
        System.out.printf("%-5s %-10s %-20s %-10s %-5s %-7s %-5s %-8s%n",
                "ID", "RollNo", "Name", "Course", "Year", "Marks", "Grade", "Status");
        System.out.println("-------------------------------------------------------------------------");
    }

    private static void printStudentsList(List<Student> students) {
        if (students == null || students.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        printStudentsHeader();
        for (Student s : students) {
            System.out.println(s);
        }
    }
}