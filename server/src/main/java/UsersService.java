public interface UsersService {
    public String getUserByLoginAndPassword(String login, String password);
    public void addUser(String login, String password, String nick);

    boolean searchByLogin(String login);
    boolean searchByNick(String nickname);
}
