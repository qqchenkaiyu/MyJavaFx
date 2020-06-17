package ch.makery.address.view;

import ch.makery.address.anotation.DefaultView;
import ch.makery.address.model.Context;
import ch.makery.address.model.ServerConfig;
import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.*;
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
import org.apache.commons.io.IOUtils;
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
    void close(ActionEvent event) {
        System.out.println("close");
    }
    @FXML
    private ListView<ServerConfig> serverList;
    private File defaultServerConfig;
    private Context context;
    public RootController() {
        System.out.println("我被创建了 "+this);
    }

    @FXML
   @SneakyThrows
    void 添加服务器(ActionEvent event) {
        ServerConfig serverConfig = new ServerConfig();
        DialogController controller =
                mainApp.openEditDialogForResult("添加服务器", "ServerConfig.fxml", serverConfig);
        if(controller.okClicked){
            serverList.getItems().add(serverConfig);
            FileUtil.writeObject(defaultServerConfig,serverList.getItems());
        }

    }
    @FXML
    void 打开kafka客户端(ActionEvent event) {
        DialogController kafka客户端 = mainApp.openDialogForResult("kafka客户端", "kafkaClient.fxml");
    }

    @FXML
    @SneakyThrows
    void 复制服务器(ActionEvent event) {
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if(serverConfig==null){
            DialogUtils.AlertInfomation("必须先选中服务器才能复制");
            return;
        }
        ServerConfig clone = JSON.parseObject(JSON.toJSONString(serverConfig),ServerConfig.class);
        DialogController controller =
                mainApp.openEditDialogForResult("复制服务器", "ServerConfig.fxml", clone);
        if(controller.okClicked){
            serverList.getItems().add(clone);
        }
    }
    @SneakyThrows
    @FXML
    void 生成覆盖率报告(ActionEvent event) {
        for (ServerConfig serverConfig : getSelectedServerConfigs()) {
            //下载文件到本地
            ChannelSftp channelSftp = getSftpChannel(serverConfig);
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(getContext().getCoveragePath());
            for (ChannelSftp.LsEntry file : files) {
                if(!file.getAttrs().isDir()) {
                    Path servicePath = new File("覆盖率文件").toPath();
                    if( Files.notExists(servicePath)){
                        Files.createDirectory(servicePath);
                    }

                    String destFile = "覆盖率文件/" + file.getFilename();
                    channelSftp.get(context.getCoveragePath()+"/"+file.getFilename(),
                            destFile);
                }
            }
        }
        Runtime.getRuntime().exec(context.getCoverageCmd());
        //执行命令生成报告
    }
    public List<ServerConfig> getSelectedServerConfigs() {
        ObservableList<ServerConfig> items = getServerList().getItems();
        return items.stream()
                .filter(serverConfig -> serverConfig.getSelected().get()).collect(
                        Collectors.toList());
    }
    @SneakyThrows
    @FXML
    void 首选项设置(ActionEvent event) {
        DialogController controller =
                mainApp.openEditDialogForResult("编辑首选项", "ContextController.fxml", context);
        if(controller.isOkClicked()){
            FileUtil.writeObject(preferFile,context);
        }

    }
    @FXML
    void 编辑服务器(ActionEvent event) {
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if(serverConfig==null){
            DialogUtils.AlertInfomation("必须先选中服务器才能编辑");
            return;
        }
        DialogController controller =
                mainApp.openEditDialogForResult("编辑服务器", "ServerConfig.fxml", serverConfig);
        serverList.refresh();
        if(controller.isOkClicked()){
            FileUtil.writeObject(defaultServerConfig,serverList.getItems());
        }

    }

    @FXML
    void 删除服务器(ActionEvent event) {
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if(serverConfig==null){
            DialogUtils.AlertInfomation("必须先选中服务器才能删除");
            return;
        }
        serverList.getItems().remove(serverConfig);
    }

    @FXML
    @SneakyThrows
    void 保存服务器配置(ActionEvent event) {
        FileUtil.writeObject(defaultServerConfig,serverList.getItems());
    }
    @FXML
    @SneakyThrows
    void 服务器时间同步(ActionEvent event) {
        List<ServerConfig> configs = getSelectedServerConfigs();
        if(configs.size()==0){
            DialogUtils.AlertInfomation("必须先选中服务器才能同步");
            return;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        for (ServerConfig serverConfig : configs) {
            String execResult = getExecResult(serverConfig,
                    "date -s " + dateTimeFormatter.print(System.currentTimeMillis()));
            DialogUtils.AlertInfomation("同步成功 服务器时间为"+getExecResult(serverConfig,"date"));
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
        if(!configFile.exists()){
            return;
        }
        List parse = FileUtil.readArray(configFile, ServerConfig.class);
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
        defaultServerConfig=new File(dir+"/"+"server.json");
        loadConfig(defaultServerConfig);
        preferFile=new File(dir+"/"+"predefine.json");
        if(!preferFile.exists()) {
            context=new Context();
            Files.write(preferFile.toPath(),JSON.toJSONString(context).getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE_NEW);

        }else {
            context = JSON.parseObject(new String(Files.readAllBytes(preferFile.toPath())),
                    Context.class);
            if(context==null){
                context=new Context();
                Files.write(preferFile.toPath(),JSON.toJSONString(context).getBytes(StandardCharsets.UTF_8),StandardOpenOption.WRITE);
            }

        }

    }
    @SneakyThrows
    public Session getRootSession(ServerConfig selectedItem) {
            if(selectedItem.getRootUsername()==null){
                throw new Exception("必须提供root用户名密码才能抓包");
            }
            return createNewSession(selectedItem);

    }

    private Session createNewSession(ServerConfig selectedItem) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(selectedItem.getRootUsername(), selectedItem.getIp(), selectedItem.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(selectedItem.getRootPassword());
        session.connect();
        return session;
    }

    @SneakyThrows
    public String getExecResult(ServerConfig selectedItem,String cmd) {
        Session session = getRootSession(selectedItem);
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(cmd);
        log.info("执行命令--{}",cmd);
        channel.connect();
        return new String(IOUtils.toByteArray(channel.getInputStream()),StandardCharsets.UTF_8);
    }
    @SneakyThrows
    public ChannelSftp getSftpChannel(ServerConfig selectedItem) {
            return createNewChannel(selectedItem);
    }

    private ChannelSftp createNewChannel(ServerConfig selectedItem) throws JSchException {
        Session session = getRootSession(selectedItem);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }
}