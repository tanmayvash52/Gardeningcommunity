import java.sql.Connection;
import util.DatabaseConnection;

public class ConnectionTest {

    public static void main(String[] args) {

        try {
            Connection con = DatabaseConnection.getConnection();

            System.out.println("Database connection successful!");
            System.out.println("Connected to: " + con.getCatalog());

            con.close();

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}