package ch.makery.address.view;

import ch.makery.address.model.ServiceConfig;
import ch.makery.address.util.EditDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EditServerConfigController extends EditDialogController<ServiceConfig>{

    @FXML
    private TextField 服务器lib路径;

    @FXML
    private TextField 服务名;

    @FXML
    private TextArea 本地换包路径;

    @FXML
    private TextField 日志路径;
    @FXML
    private TextField 覆盖率文件地址;
    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void save() {
obj.setLogPath(日志路径.getText());
        obj.setCoveragePath(覆盖率文件地址.getText());
        obj.setServiceName(服务名.getText());
        obj.setRemoteLibDir(服务器lib路径.getText());
        obj.setLocalFiles(本地换包路径.getText());
    }

    @Override
    public void initController() {
        覆盖率文件地址.setText(obj.getCoveragePath());
        日志路径.setText(obj.getLogPath());
        服务名.setText(obj.getServiceName());
        服务器lib路径.setText(obj.getRemoteLibDir());
        本地换包路径.setText(obj.getLocalFiles());
    }
}
