import java.sql.*;

public class Messages {
    private PreparedStatement addMesStm;
    private PreparedStatement getHistoryStm;
    private Connection conn;

    public Messages(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/networkchat?currentSchema=networkchat", "root", "root");
            addMesStm = conn.prepareStatement("INSERT INTO networkchat.messages(sender, receiver, message) VALUES(?, ?, ?)");
            getHistoryStm = conn.prepareStatement("SELECT * FROM networkchat.messages WHERE (receiver = ? OR receiver = 0 OR sender = ?) LIMIT 100");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void addMessage(String message, Integer sender, Integer receiver) throws SQLException {
        addMesStm.setInt(1, sender);
        addMesStm.setInt(2, receiver);
        addMesStm.setString(3, message);
        addMesStm.executeUpdate();
    }

    public ResultSet getHistory(Integer receiver) throws SQLException {
        getHistoryStm.setInt(1, receiver);
        getHistoryStm.setInt(2, receiver);
        ResultSet result = getHistoryStm.executeQuery();
        return result;
    }
}
