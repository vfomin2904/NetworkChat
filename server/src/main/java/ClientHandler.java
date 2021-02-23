import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler {

    public Server server;
    public Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;


    public ClientHandler(Server server, Socket socket) throws IOException {

        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        server.subscribe(this);

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        if (msg.equals(Commands.END)) {
                            System.out.println("Client disconnected");
                            server.unsubscribe(this);
                            break;
                        } else if(msg.startsWith(Commands.AUTH)){
                            String[] auth = msg.split("\s", 3);
                            if(auth.length == 3) {
                                nick = server.authorize(auth[1], auth[2]);
                                if(!nick.equals("")) {
                                    out.writeUTF(Commands.AUTH_OK+" " + nick);
                                } else{
                                    out.writeUTF(Commands.AUTH_ERROR);
                                }
                            }
                        }
//                        else if(msg.startsWith("/w")){
//                            String[] message = msg.split("\s", 3);
//                            if(message.length == 3) {
//                                server.sendPersonalMessage(message[1], message[2], nick);
//                            }
//                        }
                        continue;
                    }
                    System.out.println("Client: " + msg);
                    server.sendMsg(msg);

                }
            } catch (IOException e) {
//                    e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    server.unsubscribe(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void sendPersonalMsg(String nickname, String message, String sender){
//        if(nick.equals(nickname)){
//            sendMsg(sender+"(personal) : "+message);
//        } else if(nick.equals(sender)){
//            sendMsg(sender+"(personal for "+nickname+") : "+message);
//        }
//    }


}
