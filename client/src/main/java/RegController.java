import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {

    private Controller mainController;

    @FXML
    public TextArea regMessage;
    public TextField login;
    public PasswordField pass;
    public TextField nickname;

    public void registration(ActionEvent actionEvent) {
        String log = login.getText().trim();
        String password = pass.getText().trim();
        String nick = nickname.getText().trim();
        reg(log, password, nick);
    }

    public void reg(String log, String password, String nick){
        regMessage.setVisible(true);
        mainController.reg(log, password, nick);
    }


    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void printRegInfo(String message){
        regMessage.setText(message);
    }
}
