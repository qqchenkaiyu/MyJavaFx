package ch.makery.address.view;

import ch.makery.address.anotation.DefaultView;
import ch.makery.address.model.Context;
import ch.makery.address.model.ServerConfig;
import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 *
 */
@DefaultView("ServiceOverview.fxml")
@Data
public class RootController extends Controller {
    private HashMap<String, Session> sessionMap=new HashMap<>();
    private HashMap<String, Session> rootSessionMap=new HashMap<>();
    private HashMap<String, ChannelExec> execMap=new HashMap<>();
    private HashMap<String, ChannelSftp> sftpMap=new HashMap<>();
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

        //执行命令生成报告
        //打开浏览器查看
    }
    public List<ServerConfig> getSelectedServerConfigs() {
        RootController controller = mainApp.getRootController();
        ObservableList<ServerConfig> items = controller.getServerList().getItems();
        return items.stream()
                .filter(serverConfig -> serverConfig.getSelected().get()).collect(
                        Collectors.toList());
    }
    @SneakyThrows
    @FXML
    void 首选项设置(ActionEvent event) {
        ServerConfig serverConfig = new ServerConfig();
        DialogController controller =
                mainApp.openEditDialogForResult("添加服务器", "ServerConfig.fxml", serverConfig);
        if(controller.okClicked){
            String jsonString = JSON.toJSONString(serverList.getItems());
            defaultServerConfig.deleteOnExit();
            Files.write(defaultServerConfig.toPath(),jsonString.getBytes(), StandardOpenOption.CREATE);
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
        String jsonString = JSON.toJSONString(serverList.getItems());
        Files.writeString(defaultServerConfig.toPath(),jsonString, StandardOpenOption.WRITE);
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
        byte[] readAllBytes = Files.readAllBytes(configFile.toPath());
        List<ServerConfig> parse =
                JSON.parseArray(new String(readAllBytes, StandardCharsets.UTF_8),
                        ServerConfig.class);
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
    }
    @SneakyThrows
    public Session getRootSession(ServerConfig selectedItem) {
        if(rootSessionMap.containsKey(selectedItem.getIp())){
            return rootSessionMap.get(selectedItem.getIp());
        }else {
            if(selectedItem.getRootUsername()==null){
                throw new Exception("必须提供root用户名密码才能抓包");
            }
            JSch jsch = new JSch();
            Session session = jsch.getSession(selectedItem.getRootUsername(), selectedItem.getIp(), selectedItem.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(selectedItem.getRootPassword());
            session.connect();
            rootSessionMap.put(selectedItem.getIp(),session);
            return session;
        }
    }
    @SneakyThrows
    public String getExecResult(ServerConfig selectedItem,String cmd) {
        Session session = getRootSession(selectedItem);
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(cmd);
        channel.connect();
        byte[] bytes = channel.getInputStream().readAllBytes();
        return new String(bytes,StandardCharsets.UTF_8);
    }
    @SneakyThrows
    public ChannelSftp getSftpChannel(ServerConfig selectedItem) {
        if(sftpMap.containsKey(selectedItem.getIp())){
            return sftpMap.get(selectedItem.getIp());
        }else {
            Session session = getRootSession(selectedItem);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            sftpMap.put(selectedItem.getIp(),channel);
            return channel;
        }

    }
}