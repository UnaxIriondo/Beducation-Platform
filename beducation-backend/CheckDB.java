import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=beducation;encrypt=false;trustServerCertificate=true;";
        String user = "sa";
        String pass = "BeducationPass123!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("--- USERS ---");
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT id, email, auth0_id FROM users")) {
                while (rs.next()) System.out.println(rs.getLong(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
            }
            System.out.println("--- STUDENTS ---");
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT id, user_id, invitation_email FROM students")) {
                while (rs.next()) System.out.println(rs.getLong(1) + " | " + rs.getObject(2) + " | " + rs.getString(3));
            }
        }
    }
}
