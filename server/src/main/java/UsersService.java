import java.sql.SQLException;

public interface UsersService {
    public String getUserByLoginAndPassword(String login, String password) throws SQLException;
    public void addUser(String login, String password, String nick) throws SQLException;

    boolean searchByLogin(String login) throws SQLException;
    boolean searchByNick(String nickname) throws SQLException;
}
