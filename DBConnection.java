import java.sql.*;

public class DBConnection {
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/college",
            "root", "your_password");
    }
}
