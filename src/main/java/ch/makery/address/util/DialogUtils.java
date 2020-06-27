package ch.makery.address.util;

import com.google.common.collect.EvictingQueue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class DialogUtils {
    public static Map<String, String> alertInputWindow(List<String> list) {
        Stage window = new Stage();
        window.setTitle("输入窗口");
        //阻塞其他窗口
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(300);
        window.setMinHeight(150);
        ObservableList<CMD> cmds = FXCollections.observableArrayList();
        for (String str : list) {
            cmds.add(new CMD(str, ""));
        }
        TableView<CMD> table = new TableView<>();
        table.setEditable(true);
        TableColumn<CMD, String> firstNameCol =
                new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        TableColumn<CMD, String> lastNameCol =
                new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        lastNameCol.setCellFactory(TextFieldTableCell.<CMD>forTableColumn());
        lastNameCol.setOnEditCommit(
                (CellEditEvent<CMD, String> t) -> {
                    ((CMD) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                    ).setValue(t.getNewValue());
                });
        table.setItems(cmds);
        table.getColumns().addAll(firstNameCol, lastNameCol);
        Scene scene = new Scene(table);
        window.setScene(scene);
        //使用showAndWait()先处理这个窗口，而如果不处理，main中的那个窗口不能响应
        window.showAndWait();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (CMD cmd : cmds) {
            hashMap.put(cmd.getName(), cmd.getValue());
        }
        return hashMap;


    }

    public static void AlertInfomation(String info) {
        Alert information = new Alert(Alert.AlertType.INFORMATION, info);
        information.setTitle("Warning");
        information.setHeaderText(null);
        information.showAndWait();
    }


    public static void handleError(Thread t, Throwable e) {
        InvocationTargetException cause = (InvocationTargetException) e.getCause();
        e = cause.getTargetException();
        String format = getErrInfo(t, e);
        log.error(format);
        Alert information = new Alert(Alert.AlertType.ERROR, e.getMessage());
        information.setTitle("Error");
        information.setHeaderText(null);
        information.setResizable(true);
        information.showAndWait();
    }

    private static String getErrInfo(Thread t, Throwable e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        return String.format("线程%s发生异常,异常信息%s", t.getName(), byteArrayOutputStream.toString());
    }

    public static Optional<ButtonType> AlertConfirm(String info) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, info);
        confirmation.setHeaderText(null);
        Optional<ButtonType> result = confirmation.showAndWait();
        return result;

    }

    public static File getXLSFile(String title) {
        return getSpecificFile(title, "XSLX files (*.xlsx)", "*.xlsx");
    }

    public static File getPropertiesFile(String title) {
        return getSpecificFile(title, "Propertie files (*.properties)", "*.properties");
    }

    public static File getSpecificFile(String title, String display, String after) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(display, after);
        fileChooser.getExtensionFilters().add(extFilter);
        File showOpenDialog = fileChooser.showOpenDialog(new Stage());
        return showOpenDialog;
    }

    public static File saveFile(String title, String ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(ext, ext);
        fileChooser.getExtensionFilters().add(extFilter);
        File showOpenDialog = fileChooser.showSaveDialog(new Stage());
        return showOpenDialog;
    }

    public static File getFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File showOpenDialog = fileChooser.showOpenDialog(new Stage());
        return showOpenDialog;
    }

    public static File getDirectory(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        File showDialog = directoryChooser.showDialog(new Stage());
        if (showDialog == null) return null;
        return showDialog;
    }

    public static void showContent(String content, TextArea textArea, EvictingQueue<Object> queue) {
        queue.add(content);
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        for (Object o : queue) {
            stringJoiner.add(o.toString());
        }
        textArea.clear();
        textArea.appendText(stringJoiner.toString());

    }
}
