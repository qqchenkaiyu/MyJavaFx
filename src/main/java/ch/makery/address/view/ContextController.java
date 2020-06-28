package ch.makery.address.view;

import ch.makery.address.model.Context;
import ch.makery.address.util.EditDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ContextController extends EditDialogController<Context> {
    @FXML
    private TextField 文本编辑器;
    @FXML
    private TextField 覆盖率路径;
    @FXML
    private TextField 覆盖率命令;
    @FXML
    private TextField kafka集群位置;
    @FXML
    private TextField kafka客户端位置;
 @FXML
    private TextField 重定义地址;
    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void save() {
        obj.setTextEditor(文本编辑器.getText());
        obj.setCoveragePath(覆盖率路径.getText());
        obj.setCoverageCmd(覆盖率命令.getText());
        obj.setKafkaServers(kafka集群位置.getText());
        obj.setKafkaBin(kafka客户端位置.getText());
         obj.setRedefinePath(重定义地址.getText());
    }

    @Override
    public void initController() {
        文本编辑器.setText(obj.getTextEditor());
        覆盖率路径.setText(obj.getCoveragePath());
        覆盖率命令.setText(obj.getCoverageCmd());
        kafka集群位置.setText(obj.getKafkaServers());
        kafka客户端位置.setText(obj.getKafkaBin());
         重定义地址.setText(obj.getRedefinePath());
    }
}
