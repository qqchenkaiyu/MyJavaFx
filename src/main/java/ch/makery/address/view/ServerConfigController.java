package ch.makery.address.view;


import ch.makery.address.model.ServerConfig;
import ch.makery.address.util.EditDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ServerConfigController extends EditDialogController<ServerConfig> {

    @FXML
    private TextField 密码;

    @FXML
    private TextField 用户名;

    @FXML
    private TextField 端口;

    @FXML
    private TextField 服务器地址;

    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void save() {
        obj.setIp(服务器地址.getText());
        obj.setServicePassword(密码.getText());
        obj.setPort(Integer.valueOf(端口.getText()));
        obj.setServiceUsername(用户名.getText());
    }

    @Override
    public void initController() {

        密码.setText(obj.getServicePassword());
        用户名.setText(obj.getServiceUsername());
        端口.setText(String.valueOf(obj.getPort()));
        服务器地址.setText(obj.getIp());
    }
}
