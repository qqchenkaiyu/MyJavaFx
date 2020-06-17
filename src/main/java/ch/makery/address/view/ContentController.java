package ch.makery.address.view;

import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.EditDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ContentController extends EditDialogController {

    @FXML
    private TextArea content;

    @Override
    public void initController() {
        content.setText(obj.toString());
    }

    @Override
    public boolean isInputValid() {
        return false;
    }

    @Override
    public void save() {

    }
}
