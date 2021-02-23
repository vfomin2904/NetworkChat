import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final int PORT = 8981;
    private List<ClientHandler> clients = new ArrayList<>();
    private Map<LinkedList<String>, String> users = new HashMap<>();

    Server() {
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
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        System.out.println("Клиент покинул чат");
    }

    public void sendMsg(String msg) throws IOException {
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public String authorize(String login, String pass){
        return users.getOrDefault(new LinkedList<>(Arrays.asList(login, pass)), "");
    }

    private void fillUsers(){
        users.put(new LinkedList<>(Arrays.asList("qwe", "qwe")), "nick1");
        users.put(new LinkedList<>(Arrays.asList("asd", "asd")), "nick2");
        users.put(new LinkedList<>(Arrays.asList("zxc", "zxc")), "nick3");
    }

//    public void sendPersonalMessage(String nick, String message, String sender){
//        for (ClientHandler client : clients) {
//            client.sendPersonalMsg(nick, message, sender);
//        }
//    }
}
