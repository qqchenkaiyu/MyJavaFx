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
    private TextField 别名;
    @FXML
    private TextField 端口;

    @FXML
    private TextField 服务器地址;

    @FXML
    private TextField 进程用户名;

    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void save() {
        obj.set别名(别名.getText());
        obj.setIp(服务器地址.getText());
        obj.setRootPassword(密码.getText());
        obj.setPort(Integer.valueOf(端口.getText()));
        obj.setRootUsername(用户名.getText());
        obj.setServiceUsername(进程用户名.getText());
    }

    @Override
    public void initController() {
        别名.setText(obj.get别名());
        密码.setText(obj.getRootPassword());
        用户名.setText(obj.getRootUsername());
        端口.setText(obj.getPort()==null?"":obj.getPort().toString());
        服务器地址.setText(obj.getIp());
        进程用户名.setText(obj.getServiceUsername());
    }
}
