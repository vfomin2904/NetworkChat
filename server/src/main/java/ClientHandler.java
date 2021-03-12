import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientHandler {

    public Server server;
    public Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick = "";


    public ClientHandler(Server server, Socket socket) throws IOException {

        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        socket.setSoTimeout(120000);

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
                                    if(server.checkConnect(nick)){
                                        server.subscribe(this);
                                        out.writeUTF(Commands.AUTH_OK+" " + nick);
                                        socket.setSoTimeout(0);
                                    }
                                    else{
                                        out.writeUTF(Commands.USER_ALREADY_CONNECT+" " + nick);
                                    }
                                } else{
                                    out.writeUTF(Commands.AUTH_ERROR);
                                }
                            }
                        }
                        else if(msg.startsWith(Commands.PRIVATE_MESSAGE)){
                            String[] message = msg.split("\s", 3);
                            if(message.length == 3) {
                                server.sendPersonalMessage(message[1], message[2], nick, this);
                            }
                        }
                        else if(msg.startsWith(Commands.REG)){
                            String[] message = msg.split("\s", 4);
                            if(message.length == 4) {
                                server.reg(message[1], message[2], message[3], this);
                            }
                        }
                        else if(msg.startsWith(Commands.CHANGE_NICK)){
                            String[] message = msg.split("\s", 2);
                            if(message.length == 2) {
                                server.changeNick(message[1],this);
                            }
                        }
                        continue;
                    }
                    System.out.println("Client: " + msg);
                    server.sendMsg(msg);

                }
            }catch(SocketTimeoutException e){
                try {
                    out.writeUTF(Commands.END);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            catch (IOException e) {
                    e.printStackTrace();
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

    public boolean sendPersonalMsg(String nickname, String message, String sender, ClientHandler senderHandler){
        if(nick.equals(nickname)){
            sendMsg("["+sender+"](personal) : "+message);
            senderHandler.sendMsg("["+sender+"] to ["+nickname+"] : "+message);
            return true;
        }
        return false;
    }


    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
