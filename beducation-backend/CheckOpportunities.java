import java.sql.*;

public class CheckOpportunities {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=beducation;encrypt=false;trustServerCertificate=true;";
        String user = "sa";
        String pass = "BeducationPass123!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("--- START OPPORTUNITIES ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, title, status, spots_available, country FROM opportunities")) {
                while (rs.next()) {
                    System.out.println("OPP_DATA:" + rs.getLong("id") + 
                                       "|STAT:" + rs.getString("status") + 
                                       "|SPOTS:" + rs.getInt("spots_available") + 
                                       "|TITLE:" + rs.getString("title"));
                }
            }
            System.out.println("--- END OPPORTUNITIES ---");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
