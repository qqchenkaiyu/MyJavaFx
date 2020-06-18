package ch.makery.address.view;

import ch.makery.address.model.ServerConfig;
import ch.makery.address.model.ServiceConfig;
import ch.makery.address.util.Controller;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

@Data
public class ServiceController extends Controller {
    File ServiceConfig = new File(System.getProperty("user.dir") + "/serviceConfig.json");
    ObservableList<ServiceConfig> serviceConfigs;
    PrintWriter printWriter;
    ChannelShell channelShell;
    ServiceConfig currentService;
    private RootController rootController;
    @FXML
    private Button 抓包开始;
    @FXML
    private ComboBox<ServiceConfig> 服务名称;
    private boolean isCapturing;

    @FXML
    void 抓包开始(ActionEvent event) throws Exception {
        for (ServerConfig serverConfig : rootController.getSelectedServerConfigs()) {
            if (isCapturing) {
                printWriter.println("exit");
                printWriter.flush();
                printWriter.close();
                channelShell.disconnect();
                ChannelSftp channelSftp = rootController.getSftpChannel(serverConfig);
                channelSftp.get("/root/抓包.cap",
                        currentService.getServiceName() + ".cap");
                isCapturing = false;
                抓包开始.setText("抓包开始");
                DialogUtils.AlertInfomation("抓包完成");
            } else {
                isCapturing = true;
                Session rootSession = rootController.getRootSession(serverConfig);
                channelShell = (ChannelShell) rootSession.openChannel("shell");
                channelShell.connect();
                CompletableFuture.runAsync(() -> {
                    while (channelShell.isConnected()) {
                        try {
                            Thread.sleep(200);
                            IOUtils.copy(channelShell.getInputStream(),System.out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                printWriter = new PrintWriter(channelShell.getOutputStream());
                printWriter.println("tcpdump -i any host " + serverConfig.getIp() + " -w /root/抓包.cap");
                printWriter.flush();
                抓包开始.setText("结束抓包");
            }

        }
    }

    @SneakyThrows
    @Override
    public void initController() {

        rootController = mainApp.getRootController();
        String readString = new String(Files.readAllBytes(ServiceConfig.toPath()));
        serviceConfigs=FXCollections
                .observableArrayList(JSON.parseArray(readString, ServiceConfig.class));
        服务名称.setItems(serviceConfigs);
        服务名称.getSelectionModel().select(0);
        currentService = 服务名称.getSelectionModel().getSelectedItem();
        服务名称.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("服务切换成" + newValue.getDisplayName());
                    currentService = newValue;
                });
        for (ServiceConfig serviceConfig : serviceConfigs) {
            File file = new File(serviceConfig.getServiceName());
            if(!file.exists()){
                file.mkdir();
            }
        }

    }

    @FXML
    void 启动服务(ActionEvent event) {
        rootController.getSelectedServerConfigs().stream().forEach(serverConfig -> {
            String res = rootController.getExecResult(serverConfig,
                    currentService.getStartCmd());
        });
    }

    @FXML
    void 停止服务(ActionEvent event) {
        rootController.getSelectedServerConfigs().stream().forEach(serverConfig -> {
            String res = rootController.getExecResult(serverConfig,
                    currentService.getStopCmd());
        });

    }

    @SneakyThrows
    @FXML
    void 配置服务(ActionEvent event) {
        DialogController controller =
                mainApp.openEditDialogForResult("编辑服务信息", "EditServiceConfig.fxml", currentService);
        if(controller.isOkClicked()){
            FileUtil.writeObject(ServiceConfig,serviceConfigs);
        }
    }
    @FXML
    @SneakyThrows
    void 添加服务(ActionEvent event) {
        ServiceConfig serviceConfig = new ServiceConfig();
        DialogController controller =
                mainApp.openEditDialogForResult("添加服务信息", "EditServiceConfig.fxml", serviceConfig);
        if(controller.isOkClicked()){
            serviceConfigs.add(serviceConfig);
            FileUtil.writeObject(ServiceConfig,serviceConfigs);
        }
    }

    @FXML
    @SneakyThrows
    void 删除服务(ActionEvent event) {
        Optional<ButtonType> buttonType = DialogUtils.AlertConfirm("确定要删除吗?");
        ButtonType buttonType1 = buttonType.get();
        if(buttonType1.getText().equals("确定")){
            serviceConfigs.remove(currentService);
            FileUtil.writeObject(ServiceConfig,serviceConfigs);
        }



    }
    @SneakyThrows
    @FXML
    void 采集日志压缩包(ActionEvent event) {
        for (ServerConfig serverConfig : rootController.getSelectedServerConfigs()) {
            ChannelSftp channelSftp = rootController.getSftpChannel(serverConfig);
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(currentService.getLogPath());
            for (ChannelSftp.LsEntry file : files) {
                if(file.getFilename().contains(currentService.getLoggzPatten())){
                    channelSftp.get(currentService.getLogPath()+"/"+file.getFilename(),
                            currentService.getServiceName() +"/"+ serverConfig.getIp()+file.getFilename());
                }
            }
        }
    }
    @FXML
    @SneakyThrows
    void 采集日志(ActionEvent event) {
        for (ServerConfig serverConfig : rootController.getSelectedServerConfigs()) {
            ChannelSftp channelSftp = rootController.getSftpChannel(serverConfig);
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(currentService.getLogPath());
            for (ChannelSftp.LsEntry file : files) {
                if(file.getFilename().endsWith("log")){
                    channelSftp.get(currentService.getLogPath()+"/"+file.getFilename(),
                            currentService.getServiceName() +"/"+ serverConfig.getIp()+file.getFilename());
                    Runtime.getRuntime().exec(rootController.getContext().getTextEditor()+" " + currentService.getServiceName() +"/"+ serverConfig.getIp()+file.getFilename());
                }
            }
        }
    }
    @FXML
    @SneakyThrows
    void 上传本地jar包(ActionEvent event) {
        for (ServerConfig serverConfig : rootController.getSelectedServerConfigs()) {
            ChannelSftp channelSftp = rootController.getSftpChannel(serverConfig);
            String replaceAll = currentService.getLocalFiles().replaceAll("\\n", "");
            String[] files = replaceAll.split(";");
            for (String s : files) {
                File file = new File(s.replaceAll(System.lineSeparator(),""));
                String name = file.getName();
                if(file.exists()) {
                    channelSftp.put(s, currentService.getRemoteLibDir() + "/" + name);
                    DialogUtils.AlertInfomation("上传成功"+file.getName());
                }else {
                    DialogUtils.AlertInfomation("文件不存在"+file.getName());
                }
            }

        }
    }

    @FXML
    @SneakyThrows
    void 采集堆栈(ActionEvent event) {
        for (ServerConfig serverConfig : rootController.getSelectedServerConfigs()) {
            String pid = rootController.getExecResult(serverConfig,
                    "ps -ef|grep " + currentService.getServiceName() +
                            " |grep -v grep|awk '{print $2 }'");
            String execResult = rootController.getExecResult(serverConfig,
                    "su "+serverConfig.getServiceUsername()+" -c 'jmap -dump:format=b,file=/home/" + serverConfig.getServiceUsername() +
                            "/下载/heap.bin " + pid+"'");
            System.out.println(execResult);
            ChannelSftp channelSftp = rootController.getSftpChannel(serverConfig);
            channelSftp.get("/home/" + serverConfig.getServiceUsername() + "/下载/heap.bin",
                    currentService.getServiceName() + serverConfig.getIp() + ".bin");
            DialogUtils.AlertInfomation("下载成功！");

        }
    }


    @FXML
    void 获取进程id(ActionEvent event) {
        rootController.getSelectedServerConfigs().stream().forEach(serverConfig -> {
            String execResult = rootController.getExecResult(serverConfig,
                    "ps -ef|grep " + currentService.getServiceName() +
                            " |grep -v grep|awk '{print $2 }'");
            DialogUtils.AlertInfomation("此服务的进程id是" + execResult);
        });
    }
}