import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;

public class Server {

    private final int PORT = 8981;
    private List<ClientHandler> clients = new ArrayList<>();
    private UsersService users;
    private UsersRepository repository;
    private Messages msgRepository;

    Server() {
//        users = new UsersListService();
        repository = new UsersRepository();
//        fillUsers();
        msgRepository = new Messages();
        try (ServerSocket server = new ServerSocket(PORT);) {
            System.out.println("Server started");


            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            repository.disconnect();
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
        msgRepository.addMessage(msg, , 0);
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public String authorize(String login, String pass) {
        try {
            return repository.getUserByLoginAndPassword(login, pass);
        } catch (SQLException e) {
            return "";
        }
    }

//    private void fillUsers(){
//        users.addUser("qwe", "qwe", "nick1");
//        users.addUser("asd", "asd", "nick2");
//        users.addUser("zxc", "zxc", "nick3");
//    }

    public void sendPersonalMessage(String nick, String message, String sender, ClientHandler senderHandler) {
        boolean successSendMessage = false;
        for (ClientHandler client : clients) {
            if (client.sendPersonalMsg(nick, message, sender, senderHandler)) {
                successSendMessage = true;
                break;
            }
        }
        if (!successSendMessage) {
            senderHandler.sendMsg("Пользователь " + nick + " не в сети");
        }

    }

    public boolean checkConnect(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return false;
            }
        }
        return true;
    }

    public void sendUsersList() {
        StringBuilder msg = new StringBuilder(Commands.USERS_LIST);
        for (ClientHandler client : clients) {
            msg.append(" ").append(client.getNick());
        }
        try {
            sendMsg(msg.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reg(String login, String password, String nickname, ClientHandler client) {
        if (repository.searchByLogin(login)) {
            client.sendMsg(Commands.REG_ERROR + " Логин " + login + " уже существует");
        } else if (repository.searchByNick(nickname)) {
            client.sendMsg(Commands.REG_ERROR + " Ник " + nickname + " уже существует");
        } else {
            try {
                repository.addUser(login, password, nickname);
                client.sendMsg(Commands.REG_OK);
            } catch (SQLException throwables) {
                client.sendMsg(Commands.REG_ERROR + "Ошибка регистрации. Попробуйте еще раз");
            }

        }
    }

    public void changeNick(String newNick, ClientHandler clientHandler) {
        try{
            if(repository.searchByNick(newNick)){
                clientHandler.sendMsg("Пользователь с таким ником уже существует");
            }else{
                String oldNick = clientHandler.getNick();
                repository.updateNick(newNick, oldNick);
                sendMsg("Пользователь "+oldNick+" сменил имя на "+newNick);
                clientHandler.setNick(newNick);
                sendUsersList();
            }
        } catch (SQLException e) {
            clientHandler.sendMsg("Не удалось изменить имя пользователя");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
