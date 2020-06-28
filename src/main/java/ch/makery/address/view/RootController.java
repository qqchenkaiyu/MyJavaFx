package ch.makery.address.view;

import ch.makery.address.anotation.DefaultView;
import ch.makery.address.model.Context;
import ch.makery.address.model.ServerConfig;
import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
    private Cache<String, Object> cache;
    @FXML
    private ListView<ServerConfig> serverList;
    private File defaultServerConfig;
    private Context context;
    public RootController() {
        System.out.println("我被创建了 " + this);
    }

    public static void main(String[] args) throws SftpException {
        ChannelSftp sftpChannel = new RootController().getSftpChannel(new ServerConfig("192.168.208.128", 22, "虚拟机192.168.208.128", "chenkaiyu", "root", "qq634691", null));
        String targetDir = "\\home\\chenkaiyu\\com\\example\\demo";
        targetDir = targetDir.replaceAll("\\\\", "/");
        sftpChannel.put("F://test/com/example/demo/BBking.java", targetDir);
        SftpATTRS lstat = sftpChannel.lstat(targetDir + "/" + "BBking.java");
        System.out.println(lstat);
    }

    @FXML
    void close(ActionEvent event) {
        System.out.println("close");
    }

    @FXML
    @SneakyThrows
    void 添加服务器(ActionEvent event) {
        ServerConfig serverConfig = new ServerConfig();
        DialogController controller =
                mainApp.openEditDialogForResult("添加服务器", "ServerConfig.fxml", serverConfig);
        if (controller.okClicked) {
            try {
                Session rootSession = getRootSession(serverConfig);
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
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        if (serverConfig == null) {
            DialogUtils.AlertInfomation("必须先选中服务器才能复制");
            return;
        }
        ServerConfig clone = serverConfig.clone();
        DialogController controller =
                mainApp.openEditDialogForResult("复制服务器", "ServerConfig.fxml", clone);
        if (controller.okClicked) {
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
        if (controller.isOkClicked()) {
            FileUtil.writeObject(preferFile, context);
        }

    }

    @FXML
    void 编辑服务器(ActionEvent event) {
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
        ServerConfig clone = serverConfig.clone();
        if (serverConfig == null) {
            DialogUtils.AlertInfomation("必须先选中服务器才能编辑");
            return;
        }
        DialogController controller =
                mainApp.openEditDialogForResult("编辑服务器", "ServerConfig.fxml", serverConfig);

        if (controller.isOkClicked()) {
            try {
                Session rootSession = getRootSession(serverConfig);
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
        ServerConfig serverConfig = serverList.getSelectionModel().getSelectedItem();
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
        List<ServerConfig> configs = getSelectedServerConfigs();
        if (configs.size() == 0) {
            DialogUtils.AlertInfomation("必须先选中服务器才能同步");
            return;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        for (ServerConfig serverConfig : configs) {
            String execResult = getExecResult(serverConfig,
                    "date -s " + dateTimeFormatter.print(System.currentTimeMillis()));
            DialogUtils.AlertInfomation("同步成功 服务器时间为" + getExecResult(serverConfig, "date"));
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
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.expireAfterWrite(5, TimeUnit.MINUTES);
        cache = cacheBuilder.build();
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

    @SneakyThrows
    public Session getRootSession(ServerConfig selectedItem) {
        if (selectedItem.getRootUsername() == null) {
            throw new Exception("必须提供root用户名密码才能抓包");
        }
        return (Session) cache.get(selectedItem.getIp() + "Session", () -> createNewSession(selectedItem));
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
    public String getAsyncExecResult(ServerConfig selectedItem, String cmd) {
        Session session = getRootSession(selectedItem);
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.connect();
        log.info("执行命令--{}", cmd);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StringBuffer result = new StringBuffer();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(channel.getInputStream(),
                                StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(selectedItem.getRootUsername())) break;
                    result.append(line + System.lineSeparator());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        });
        PrintWriter printWriter = new PrintWriter(channel.getOutputStream());
        printWriter.println(cmd);
        printWriter.flush();
        printWriter.close();
        countDownLatch.await(5, TimeUnit.SECONDS);
        return result.toString();
    }

    @SneakyThrows
    public String getExecResult(ServerConfig selectedItem, String cmd) {
        String result =
                ExecShell(selectedItem, cmd);
        if (StringUtils.isEmpty(result)) {
            log.error("同步无法获得结果 改为异步");
            result = getAsyncExecResult(selectedItem, cmd);
        }
        return result;
    }

    @SneakyThrows
    public String ExecShell(ServerConfig selectedItem, String cmd) {
        Session session = getRootSession(selectedItem);
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(cmd);
        log.info("执行命令--{}", cmd);
        channel.connect();
        String result =
                new String(IOUtils.toByteArray(channel.getInputStream()), StandardCharsets.UTF_8);
        return result;
    }

    @SneakyThrows
    public ChannelSftp getSftpChannel(ServerConfig selectedItem) {
        return (ChannelSftp) cache.get(selectedItem.getIp() + "SftpChannel", () -> createNewChannel(selectedItem));
    }

    private ChannelSftp createNewChannel(ServerConfig selectedItem) throws JSchException {
        Session session = getRootSession(selectedItem);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    public boolean isFileExist(ChannelSftp sftpChannel, String path) {
        try {
            SftpATTRS lstat = sftpChannel.lstat(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void recursiveDelete(ChannelSftp sftp, String path)
            throws SftpException {
        Vector<?> entries = sftp.ls(path);
        for (Object object : entries) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
            if (entry.getFilename().equals(".")
                    || entry.getFilename().equals("..")) {
                continue;
            }
            if (entry.getAttrs().isDir()) {
                recursiveDelete(sftp, path + entry.getFilename() + "/");
            } else {
                sftp.rm(path + entry.getFilename());
            }
        }
        sftp.rmdir(path);
    }
}
