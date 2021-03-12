import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Messages {
    private PreparedStatement addMesStm;
    private Connection conn;

    public Messages(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/networkchat?currentSchema=networkchat", "root", "root");
            addMesStm = conn.prepareStatement("INSERT INTO networkchat.messages(sender, receiver, message) VALUES(?, ?, ?)");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void addMessage(String message, Integer sender, Integer receiver) throws SQLException {
        addMesStm.setString(1, message);
        addMesStm.setInt(2, sender);
        addMesStm.setInt(3, receiver);
        addMesStm.executeUpdate();
    }
}
