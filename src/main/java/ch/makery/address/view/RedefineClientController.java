package ch.makery.address.view;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.UnicodeUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import ch.makery.address.model.ServerConfig;
import ch.makery.address.model.ServiceConfig;
import ch.makery.address.util.DialogUtils;
import ch.makery.address.util.EditDialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedefineClientController extends EditDialogController<ServiceConfig> {
    String agentpath;
    RootController rootController;
    @FXML
    private TextField 输出地址;
    @FXML
    private TextArea 类文件内容;
    @FXML
    private TextField 全类名;
    private String userhome;
    private String userhomeRedifine;
    private String agentfile;
    private String linuxAgent;
    private String linuxJavaPath;
    private String linuxClassPath;
    private String preCmd;
    private ServerConfig serverConfig;
    private ChannelSftp sftpChannel;

    @SneakyThrows
    @FXML
    void 下载类文件(ActionEvent event) {

        if (StringUtils.isEmpty(全类名.getText())) {
            DialogUtils.AlertInfomation("全类名 不能为空");
            return;
        }
        rootController.getContext().setLatestClass(全类名.getText());
        FileUtil.writeObject(rootController.getPreferFile(),rootController.getContext());
        if (sftpChannel == null || !sftpChannel.isConnected())
            sftpChannel = rootController.getSftpChannel(serverConfig);
        if (!rootController.isFileExist(sftpChannel, linuxAgent)) {
            sftpChannel.put(agentpath, userhomeRedifine);
            sftpChannel.chmod(Integer.parseInt("777", 8), linuxAgent);
            if (!rootController.isFileExist(sftpChannel, linuxAgent)) {
                DialogUtils.AlertInfomation("上传 agent 失败");
                return;
            }
        }
        String cmd = preCmd +
                " compile " + 全类名.getText() + " " + userhomeRedifine + "'";
        rootController.ExecShell(serverConfig, cmd);
        String classpath = 全类名.getText().replaceAll("\\.", "/") + ".class";
        linuxClassPath = userhomeRedifine + "/" + classpath;
        String javapath = 全类名.getText().replaceAll("\\.", "/") + ".java";
        String localjavapath = 输出地址.getText() + "/" + javapath;
        if (!rootController.isFileExist(sftpChannel, linuxClassPath)) {
            DialogUtils.AlertInfomation("生成class文件 失败"+linuxClassPath);
            return;
        }
        new File(输出地址.getText() + "/" + classpath).getParentFile().mkdirs();
        sftpChannel.get(linuxClassPath, 输出地址.getText() + "/" + classpath);
        String compilecmd ="java  -jar decompiler.jar "+输出地址.getText() + "/" + classpath+" --outputdir "+输出地址.getText();
        Process ps = Runtime.getRuntime().exec(compilecmd);
        boolean status = ps.waitFor(2, TimeUnit.SECONDS);
        IOUtils.copy(new InputStreamReader(ps.getInputStream(), StandardCharsets.UTF_8), System.out);
        if (!new File(localjavapath).exists()) {
            DialogUtils.AlertInfomation("生成java文件 失败");
            return;
        }
        String context = new String(Files.readAllBytes(Paths.get(localjavapath)));
        String unicodeStr2String = UnicodeUtil.toString(context);
        FileUtil.writeContent(new File(localjavapath), unicodeStr2String);
        类文件内容.setText(unicodeStr2String);
    }

    @SneakyThrows
    @FXML
    void 上传类文件(ActionEvent event) {
        if (StringUtils.isEmpty(全类名.getText())) {
            DialogUtils.AlertInfomation("全类名 不能为空");
            return;
        }
        String javapath = 全类名.getText().replaceAll("\\.", "/") + ".java";
        linuxJavaPath = userhomeRedifine + "/" + javapath;
        if (sftpChannel == null || !sftpChannel.isConnected())
            sftpChannel = rootController.getSftpChannel(serverConfig);
        if (!rootController.isFileExist(sftpChannel, linuxAgent)) {
            sftpChannel.put(agentpath, userhomeRedifine);
            sftpChannel.chmod(Integer.parseInt("777", 8), linuxAgent);
            if (!rootController.isFileExist(sftpChannel, linuxAgent)) {
                DialogUtils.AlertInfomation("上传 agent 失败");
                return;
            }
        }
        FileUtil.writeContent(new File(输出地址.getText() + "/" + javapath), 类文件内容.getText());
        String parent = new File(linuxJavaPath).getParent().replaceAll("\\\\", "/");
        log.info("上传 {}到 {}", 输出地址.getText() + "/" + javapath, parent);
        sftpChannel.put(输出地址.getText() + "/" + javapath, parent);
        sftpChannel.chmod(Integer.parseInt("777", 8), linuxJavaPath);
        if (!rootController.isFileExist(sftpChannel, linuxJavaPath)) {
            DialogUtils.AlertInfomation("上传java文件 失败");
            return;
        }
        String cmd = preCmd +
                " redefine " + linuxJavaPath + " " + userhomeRedifine+ " " +obj.getRemoteLibDir() + "'";
        String result = rootController.ExecShell(serverConfig, cmd);
        DialogUtils.AlertInfomation(result);
    }

    @SneakyThrows
    @Override
    public void initController() {

        rootController = getMainApp().getRootController();
        String jarpath = System.getProperty("user.dir");
        agentpath = jarpath + "\\redefineAgent-1.0-SNAPSHOT.jar";
        if (!new File(agentpath).exists()) {
            log.error("agent 文件不存在");
            return;
        }
        全类名.setText(rootController.getContext().getLatestClass());
        输出地址.setText(rootController.getContext().getRedefinePath());
        Optional<ServerConfig> first = rootController.getSelectedServerConfigs().stream().findFirst();
        serverConfig = first.get();
        userhome = "/home/" + serverConfig.getServiceUsername();
        userhomeRedifine = userhome + "/redefine";
        sftpChannel = rootController.getSftpChannel(serverConfig);
        if (!rootController.isFileExist(sftpChannel, userhomeRedifine)) {
            sftpChannel.mkdir(userhomeRedifine);
            sftpChannel.chmod(Integer.parseInt("777", 8), userhomeRedifine);
        }
        agentfile = "redefineAgent-1.0-SNAPSHOT.jar";
        linuxAgent = userhomeRedifine + "/" + agentfile;
        String pid = rootController.getExecResult(serverConfig,
            "ps -ef|grep " + obj.getServiceName() +
                " |grep -v grep|awk '{print $2 }'").replaceAll("\n","");
        preCmd ="su - " + serverConfig.getServiceUsername() + " -c  'cd " + userhomeRedifine + ";java -jar " + agentfile + " attach " + pid + "  " + linuxAgent;
        dialogStage.setOnCloseRequest((event) -> {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                try {
                    if (rootController.isFileExist(sftpChannel, userhomeRedifine)) {
                        rootController.recursiveDelete(sftpChannel, userhomeRedifine + "/");
                        DialogUtils.AlertInfomation("删除临时文件成功");
                    }

                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean isInputValid() {
        return false;
    }

    @Override
    public void save() {

    }
}
