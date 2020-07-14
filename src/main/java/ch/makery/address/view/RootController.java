package ch.makery.address.view;

import ch.makery.address.anotation.DefaultView;
import ch.makery.address.model.Context;
import ch.makery.address.model.MyServerConfig;
import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cky.jsch.JschUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.AnchorPane;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@DefaultView("ServiceOverview.fxml")
@Data
@Slf4j
public class RootController extends Controller {
    private File preferFile;
    @FXML
    private AnchorPane serverPane;
    @FXML
    private ListView<MyServerConfig> serverList;
    private File defaultServerConfig;
    private Context context;
    public RootController() {
        System.out.println("我被创建了 " + this);
    }

//    public static void main(String[] args) throws SftpException {
//        ChannelSftp sftpChannel = JschUtil.getSftpChannel(new MyServerConfig("192.168.208.128", 22, "虚拟机192.168.208.128", "chenkaiyu", "root", "qq634691", null));
//        String targetDir = "\\home\\chenkaiyu\\com\\example\\demo";
//        targetDir = targetDir.replaceAll("\\\\", "/");
//        sftpChannel.put("F://test/com/example/demo/BBking.java", targetDir);
//        SftpATTRS lstat = sftpChannel.lstat(targetDir + "/" + "BBking.java");
//        System.out.println(lstat);
//    }

    @FXML
    void close(ActionEvent event) {
        System.out.println("close");
    }

    @FXML
    @SneakyThrows
    void 添加服务器(ActionEvent event) {
        MyServerConfig serverConfig = new MyServerConfig();
        DialogController controller =
                mainApp.openEditDialogForResult("添加服务器", "ServerConfig.fxml", serverConfig);
        if (controller.okClicked) {
            try {
                Session rootSession = JschUtil.getRootSession(serverConfig);
            } catch (Exception e) {
                DialogUtils.AlertInfomation("无法登陆");
                return;
            }
            serverList.getItems().add(serverConfig);
            FileUtil.writeObject(defaultServerConfig, serverList.getItems());
        }
    }

    @FXML
    void 打开kafka客户端(ActionEvent event) {
        DialogController kafka客户端 = mainApp.openDialogForResult("kafka客户端", "kafkaClient.fxml");
    }

    @FXML
    @SneakyThrows
    void 复制服务器(ActionEvent event) {
        MyServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if (serverConfig == null) {
            DialogUtils.AlertInfomation("必须先选中服务器才能复制");
            return;
        }
        MyServerConfig clone = JSONObject.parseObject(JSONObject.toJSONString(serverConfig),MyServerConfig.class);
        DialogController controller =
                mainApp.openEditDialogForResult("复制服务器", "ServerConfig.fxml", clone);
        if (controller.okClicked) {
            serverList.getItems().add(clone);
        }
    }

    @SneakyThrows
    @FXML
    void 生成覆盖率报告(ActionEvent event) {
        for (MyServerConfig serverConfig : getSelectedServerConfigs()) {
            //下载文件到本地
            ChannelSftp channelSftp = JschUtil.getSftpChannel(serverConfig);
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(getContext().getCoveragePath());
            for (ChannelSftp.LsEntry file : files) {
                if (!file.getAttrs().isDir()) {
                    Path servicePath = new File("覆盖率文件").toPath();
                    if (Files.notExists(servicePath)) {
                        Files.createDirectory(servicePath);
                    }

                    String destFile = "覆盖率文件/" + file.getFilename();
                    channelSftp.get(context.getCoveragePath() + "/" + file.getFilename(),
                            destFile);
                }
            }
        }
        Runtime.getRuntime().exec(context.getCoverageCmd());
        //执行命令生成报告
    }

    public List<MyServerConfig> getSelectedServerConfigs() {
        ObservableList<MyServerConfig> items = getServerList().getItems();
        return items.stream()
                .filter(serverConfig -> serverConfig.getSelected().get()).collect(
                        Collectors.toList());
    }

    @SneakyThrows
    @FXML
    void 首选项设置(ActionEvent event) {
        DialogController controller =
                mainApp.openEditDialogForResult("编辑首选项", "ContextController.fxml", context);
        if (controller.isOkClicked()) {
            FileUtil.writeObject(preferFile, context);
        }

    }

    @FXML
    void 编辑服务器(ActionEvent event) {
        MyServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        MyServerConfig clone = JSONObject.parseObject(JSONObject.toJSONString(serverConfig),MyServerConfig.class);
        if (serverConfig == null) {
            DialogUtils.AlertInfomation("必须先选中服务器才能编辑");
            return;
        }
        DialogController controller =
                mainApp.openEditDialogForResult("编辑服务器", "ServerConfig.fxml", serverConfig);

        if (controller.isOkClicked()) {
            try {
                Session rootSession = JschUtil.getRootSession(serverConfig);
            } catch (Exception e) {
                DialogUtils.AlertInfomation("无法登陆");
                serverConfig.setRootPassword(clone.getRootPassword());
                serverConfig.setRootUsername(clone.getRootUsername());
                serverConfig.setIp(clone.getIp());
                serverConfig.setPort(clone.getPort());
                return;
            }
            FileUtil.writeObject(defaultServerConfig, serverList.getItems());
            serverList.refresh();
        }

    }

    @FXML
    void 删除服务器(ActionEvent event) {
        MyServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if (serverConfig == null) {
            DialogUtils.AlertInfomation("必须先选中服务器才能删除");
            return;
        }
        serverList.getItems().remove(serverConfig);
    }

    @FXML
    @SneakyThrows
    void 保存服务器配置(ActionEvent event) {
        FileUtil.writeObject(defaultServerConfig, serverList.getItems());
    }

    @FXML
    @SneakyThrows
    void 服务器时间同步(ActionEvent event) {
        List<MyServerConfig> configs = getSelectedServerConfigs();
        if (configs.size() == 0) {
            DialogUtils.AlertInfomation("必须先选中服务器才能同步");
            return;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        for (MyServerConfig serverConfig : configs) {
            String execResult = JschUtil.ExecShell(serverConfig,
                    "date -s " + dateTimeFormatter.print(System.currentTimeMillis()));
            DialogUtils.AlertInfomation("同步成功 服务器时间为" + JschUtil.getExecResult(serverConfig, "date"));
        }
    }

    @SneakyThrows
    @FXML
    void 加载服务器配置(ActionEvent event) {
        File configFile = DialogUtils.getSpecificFile("获取服务器配置文件", "Json files", "*.json");
        if (configFile != null) {
            loadConfig(configFile);
        }
    }

    private void loadConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            return;
        }
        List parse = FileUtil.readArray(configFile, MyServerConfig.class);
        serverList.setItems(FXCollections.observableList(parse));
        serverList.setCellFactory(CheckBoxListCell.forListView(
                serverConfig -> {
                    BooleanProperty selected = serverConfig.getSelected();
                    return selected;
                }));
    }

    @SneakyThrows
    @Override
    public void initController() {
        String dir = System.getProperty("user.dir");
        defaultServerConfig = new File(dir + "/" + "server.json");
        loadConfig(defaultServerConfig);
        preferFile = new File(dir + "/" + "predefine.json");
        if (!preferFile.exists()) {
            context = new Context();
            Files.write(preferFile.toPath(), JSON.toJSONString(context).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

        } else {
            context = JSON.parseObject(new String(Files.readAllBytes(preferFile.toPath())),
                    Context.class);
            if (context == null) {
                context = new Context();
                Files.write(preferFile.toPath(), JSON.toJSONString(context).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
            }

        }

    }
}
