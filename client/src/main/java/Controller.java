import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class Controller implements Initializable {


    public HBox changeNick;
    public TextField newNick;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private final String IP = "localhost";
    private final int PORT = 8981;
    private String nick;
    private boolean authorize;
    private Stage regStage;
    private RegController regController;

    @FXML
    public TextField inputField;
    @FXML
    public VBox vbox;
    @FXML
    public ScrollPane scroll;
    @FXML
    public ListView<String> userList;
    public TextField login;
    public PasswordField pass;
    public Button buttonPane;
    public VBox loginForm;


    public void sendButton(ActionEvent actionEvent) {
        String msg = inputField.getText();
        if (msg.length() > 0) {
            sendMessage(msg);
            inputField.clear();
            inputField.requestFocus();
        }
    }

    public void sendMessage(String msg) {

        if (!msg.startsWith("/")) {
            msg = "[" + nick + "] : " + msg;
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
            Text f1 = new Text(" " + msg);
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
        sendMessage(Commands.AUTH + " " + userLogin + " " + userPassword);
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
                            if (msg.startsWith("/")) {
                                if (msg.startsWith(Commands.END)) {
                                    printMsg("Сервер закрыл соединение");
                                    break;
                                } else if (msg.startsWith(Commands.AUTH_OK)) {
                                    String[] words = msg.split("\s", 2);
                                    nick = words[1];
                                    authorize = true;
                                    authorizeUser();
                                } else if (msg.startsWith(Commands.AUTH_ERROR)) {
                                    nick = "";
                                    authorize = false;
                                    printMsg("Неверно введены логин/пароль");
                                } else if (msg.startsWith(Commands.USER_ALREADY_CONNECT)) {
                                    String[] words = msg.split("\s", 2);
                                    nick = words[1];
                                    authorize = false;
                                    printMsg("Пользователь " + nick + " уже в сети");
                                } else if (msg.startsWith(Commands.USERS_LIST)) {
                                    String[] users = msg.split("\s");
                                    fillUsersList(users);
                                }
                                else if (msg.startsWith(Commands.REG_ERROR)) {
                                    String[] regInfo = msg.split("\s", 2);
                                    regController.printRegInfo("Не удалось зарегистрировать пользователя: "+regInfo[1]);
                                } else if (msg.startsWith(Commands.REG_OK)) {
                                    regController.printRegInfo("Регистрация прошла успешно");
                                }
                                continue;
                            }
                            if (authorize) {
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

    private void fillUsersList(String[] users) {
        Platform.runLater(() -> {
            userList.getItems().clear();
            for (int i = 1; i < users.length; i++) {
                userList.getItems().add(users[i]);
            }
        });
    }


    private void setTitle() {
        Platform.runLater(() -> {
            Stage stage = (Stage) inputField.getScene().getWindow();
            stage.setTitle("Сетевой чат (" + nick + ")");
        });
    }

    private void authorizeUser() {
        if (authorize) {
            Platform.runLater(() -> {
                inputField.clear();
                inputField.requestFocus();
                loginForm.setVisible(false);
                loginForm.setManaged(false);
                inputField.setVisible(true);
                buttonPane.setVisible(true);
                inputField.setManaged(true);
                buttonPane.setManaged(true);
                userList.setVisible(true);
                userList.setManaged(true);
                changeNick.setManaged(true);
                changeNick.setVisible(true);
                scroll.setStyle("-fx-background-radius: 8 0 0 8;");
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


    public void selectUser(MouseEvent mouseEvent) {
        try {
            inputField.setText(Commands.PRIVATE_MESSAGE + " " + userList.getSelectionModel().getSelectedItems().get(0) + " ");
        } catch (RuntimeException e){

        }
    }

    public void showRegWindow(ActionEvent actionEvent){
        if(regStage == null){
            initRegWindow();
        }
        regStage.show();
    }

    public void initRegWindow() {

        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
            Parent root = fxmlLoader.load();
            regController = fxmlLoader.getController();
            regController.setMainController(this);

            regStage = new Stage();
            regStage.setTitle("Сетевой чат: регистрация");
            regStage.getIcons().add(new Image("image/icon.png"));
            regStage.setScene(new Scene(root, 450, 350));
            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void reg(String log, String password, String nick) {
        try {
            out.writeUTF(Commands.REG+" "+log+" "+password+" "+nick);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeNick(ActionEvent actionEvent) {
        try {
            String nick = newNick.getText();
            if(nick.trim().length()>0){
                out.writeUTF(Commands.CHANGE_NICK+" "+nick);
                newNick.clear();
            }
        } catch (IOException e) {
            printMsg("Не удалось сменить ник");
        }
    }
}

