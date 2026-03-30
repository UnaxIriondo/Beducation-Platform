import java.sql.*;

public class CheckCount {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=beducation;encrypt=false;trustServerCertificate=true;";
        String user = "sa";
        String pass = "BeducationPass123!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT status, count(*) FROM opportunities GROUP BY status")) {
                while (rs.next()) {
                    System.out.println("STATUS_COUNT:" + rs.getString(1) + " = " + rs.getInt(2));
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM opportunities WHERE status = 'APPROVED' AND spots_available > 0")) {
                if (rs.next()) {
                    System.out.println("TOTAL_APPROVED_WITH_SPOTS:" + rs.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
