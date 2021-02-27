import java.util.ArrayList;
import java.util.List;

public class UsersListService implements UsersService{

    List<Users> usersList;

    UsersListService(){
        usersList = new ArrayList<>();
    }

    public class Users{
        private String nick;
        private String login;
        private String password;

        public Users(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }
    }

    @Override
    public String getUserByLoginAndPassword(String login, String password) {
        for(Users u: usersList){
            if(u.login.equals(login) && u.password.equals(password)){
                return u.nick;
            }
        }
        return "";
    }

    @Override
    public void addUser(String login, String password, String nick){
        Users user = new Users(nick, login, password);
        usersList.add(user);
    }

    @Override
    public boolean searchByLogin(String login) {
        for(Users u: usersList){
            if(u.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean searchByNick(String nickname) {
        for(Users u: usersList){
            if(u.getNick().equals(nickname)){
                return true;
            }
        }
        return false;
    }

}
