package ch.makery.address;

import ch.makery.address.anotation.DefaultView;
import ch.makery.address.model.ServerConfig;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import ch.makery.address.util.MainApp;
import com.alibaba.fastjson.JSON;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.AnchorPane;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 *
 */
@DefaultView("ServiceOverview.fxml")
@Data
public class MyMainApp extends MainApp {
    public MyMainApp() {
        System.out.println("我被创建了 "+this);
    }
}