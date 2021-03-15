import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersRepository implements UsersService {

    private Connection conn;
    private PreparedStatement searchByLoginAndPassworStm;
    private PreparedStatement createUserStm;
    private PreparedStatement searchByLoginStm;
    private PreparedStatement searchByNickStm;
    private PreparedStatement updateNick;
    private PreparedStatement getNickById;

    UsersRepository() {

        try {
            connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/networkchat?currentSchema=networkchat", "root", "root");
        searchByLoginAndPassworStm = conn.prepareStatement("SELECT * FROM networkchat.users WHERE login = ? AND password = ?");
        createUserStm = conn.prepareStatement("INSERT INTO networkchat.users(login, password, nick) VALUES( ? , ? , ? )");
        searchByLoginStm = conn.prepareStatement("SELECT * FROM networkchat.users WHERE login = ?");
        searchByNickStm = conn.prepareStatement("SELECT * FROM networkchat.users WHERE nick = ?");
        updateNick = conn.prepareStatement("UPDATE networkchat.users SET nick = ? WHERE nick = ?");
        getNickById = conn.prepareStatement("SELECT * FROM networkchat.users WHERE id = ?");
    }

    public void disconnect(){
        try {
            conn.close();
            searchByLoginAndPassworStm.close();
            createUserStm.close();
            searchByLoginStm.close();
            searchByNickStm.close();
            updateNick.close();
            getNickById.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public String getUserByLoginAndPassword(String login, String password) throws SQLException {
        searchByLoginAndPassworStm.setString(1, login);
        searchByLoginAndPassworStm.setString(2, password);
        ResultSet result = searchByLoginAndPassworStm.executeQuery();
        result.next();
        return result.getString("nick");
    }

    @Override
    public void addUser(String login, String password, String nick) throws SQLException {

        try {
            createUserStm.setString(1, login);
            createUserStm.setString(2, password);
            createUserStm.setString(3, nick);
            createUserStm.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean searchByLogin(String login){
        try{
            searchByLoginStm.setString(1, login);
            ResultSet result = searchByLoginStm.executeQuery();
            result.next();
            result.getString("login");
        }catch(SQLException  e){
            return false;
        }
        return true;
    }

    @Override
    public boolean searchByNick(String nickname){
        try{
            searchByNickStm.setString(1, nickname);
            ResultSet result = searchByNickStm.executeQuery();
            result.next();
            result.getString("nick");
        }catch(SQLException  e){
            return false;
        }
        return true;
    }

    public Integer getIdByNick(String nick) throws SQLException {
        searchByNickStm.setString(1, nick);
        ResultSet result = searchByNickStm.executeQuery();
        result.next();
        return result.getInt("id");
    }

    public void updateNick(String newNick, String oldNick) throws SQLException {
        updateNick.setString(1, newNick);
        updateNick.setString(2, oldNick);
        updateNick.executeUpdate();
    }

    public String getNickById(Integer id) throws SQLException {
        getNickById.setInt(1, id);
        ResultSet result = getNickById.executeQuery();
        result.next();
        return result.getString("nick");
    }
}
