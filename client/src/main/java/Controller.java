import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class Controller implements Initializable {

    public TextField login;
    public PasswordField pass;
    public Button buttonPane;
    public HBox loginForm;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private final String IP = "localhost";
    private final int PORT = 8981;
    private String nick;
    private boolean authorize;

    @FXML
    public TextField inputField;
    @FXML
    public VBox vbox;
    @FXML
    public ScrollPane scroll;


    public void sendButton(ActionEvent actionEvent) {
        String msg = inputField.getText();
        if (msg.length() > 0) {
            sendMessage(msg);
            inputField.clear();
            inputField.requestFocus();
        }
    }

    public void sendMessage(String msg) {

        if(!msg.startsWith("/")){
            msg = nick+" : "+msg;
        }

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            printMsg("Соединение с сервером потеряно");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connect();
    }

    private void printMsg(String msg) {


        Platform.runLater(() -> {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm");

            Text f = new Text(formatForDateNow.format(dateNow));
            f.setStyle("-fx-font-size: 10;");
            Text f1 = new Text(" "+msg);
            f1.setStyle("-fx-font-size: 14;");

            HBox hbox = new HBox();
            hbox.getChildren().addAll(f, f1);
            hbox.setAlignment(Pos.BASELINE_LEFT);
            hbox.setMaxWidth(20.);


            StackPane stack = new StackPane(hbox);
            stack.setStyle("-fx-background-radius: 8;-fx-background-color: #B0E0E6;-fx-padding:5;-fx-max-width:20;");


            vbox.getChildren().add(stack);
            scroll.vvalueProperty().bind(vbox.heightProperty());
        });


    }

    public void login(ActionEvent actionEvent) {
        String userLogin = login.getText();
        String userPassword = pass.getText();
        sendMessage(Commands.AUTH+" " + userLogin + " " + userPassword);
    }

    private void connect() {
        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(IP, PORT);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                try {
                    while (true) {
                        try {
                            String msg = in.readUTF();
                            if(msg.startsWith("/")){
                                if(msg.startsWith(Commands.AUTH_OK)){
                                    String[] words = msg.split("\s", 2);
                                    nick = words[1];
                                    authorize = true;
                                    authorizeUser();
                                }
                                if(msg.startsWith(Commands.AUTH_ERROR)){
                                    nick = "";
                                    authorize = false;
                                    printMsg("Неверно введены логин/пароль");
                                }
                                continue;
                            }
                            if(authorize) {
                                printMsg(msg);
                            }
                        } catch (IOException e) {
//                            e.printStackTrace();
                            break;
                        }
                    }

                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                }
            } catch (IOException e) {
                System.out.println("IO Error");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        );
        t1.setDaemon(true);
        t1.start();
    }


    private void setTitle(){
        Platform.runLater(() -> {
            Stage stage = (Stage) inputField.getScene().getWindow();
            stage.setTitle("Сетевой чат ("+nick+")");
        });
    }

    private void authorizeUser(){
        if(authorize){
            Platform.runLater(() -> {
                inputField.clear();
                inputField.requestFocus();
                loginForm.setVisible(false);
                loginForm.setManaged(false);
                inputField.setVisible(true);
                buttonPane.setVisible(true);
                inputField.setManaged(true);
                buttonPane.setManaged(true);
                vbox.getChildren().removeIf(new Predicate<Node>() {
                    @Override
                    public boolean test(Node node) {
                        return true;
                    }
                });
                setTitle();
            });
        }
    }
}

