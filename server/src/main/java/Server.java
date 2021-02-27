import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final int PORT = 8981;
    private List<ClientHandler> clients = new ArrayList<>();
    private UsersService users;

    Server() {
        users = new UsersListService();
        fillUsers();
        try (ServerSocket server = new ServerSocket(PORT);) {
            System.out.println("Server started");


            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        sendUsersList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        System.out.println("Клиент покинул чат");
        sendUsersList();
    }

    public void sendMsg(String msg) throws IOException {
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public String authorize(String login, String pass){
            return users.getUserByLoginAndPassword(login, pass);
    }

    private void fillUsers(){
        users.addUser("qwe", "qwe", "nick1");
        users.addUser("asd", "asd", "nick2");
        users.addUser("zxc", "zxc", "nick3");
    }

    public void sendPersonalMessage(String nick, String message, String sender, ClientHandler senderHandler){
        boolean successSendMessage = false;
        for (ClientHandler client : clients) {
            if(client.sendPersonalMsg(nick, message, sender, senderHandler)){
                successSendMessage = true;
                break;
            }
        }
        if(!successSendMessage){
            senderHandler.sendMsg("Пользователь "+nick+" не в сети");
        }

    }

    public boolean checkConnect(String nick){
        for (ClientHandler client : clients) {
            if(client.getNick().equals(nick)){
               return false;
            }
        }
        return true;
    }

    public void sendUsersList(){
        StringBuilder msg = new StringBuilder(Commands.USERS_LIST);
        for(ClientHandler client : clients){
            msg.append(" ").append(client.getNick());
        }
        try {
            sendMsg(msg.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reg(String login, String password, String nickname, ClientHandler client) {
        if(users.searchByLogin(login)){
            client.sendMsg(Commands.REG_ERROR+" Логин "+login+" уже существует");
        } else if(users.searchByNick(nickname)){
            client.sendMsg(Commands.REG_ERROR+" Ник "+nickname+" уже существует");
        } else{
            users.addUser(login,password,nickname);
            client.sendMsg(Commands.REG_OK);
        }
    }
}
