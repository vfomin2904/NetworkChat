public interface UsersService {
    public String getUserByLoginAndPassword(String login, String password);
    public void addUser(String login, String password, String nick);
}
