package ch.makery.address.view;

import ch.makery.address.model.KafkaConfig;
import ch.makery.address.model.MyServerConfig;
import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.cky.jsch.JschUtil;
import com.cky.jsch.ServerConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.EvictingQueue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;

import java.io.File;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class KafkaClientController extends DialogController {
    KafkaConsumer<String, String> kafkaConsumer;
    EvictingQueue<Object> consumerQueue = EvictingQueue.create(50);
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    KafkaProducer<String, String> kafkaProducer;
    EvictingQueue<Object> producerQueue = EvictingQueue.create(50);
    private File kafkaConfigFile;
    private KafkaConfig kafkaConfig;
    @FXML
    private TextArea 收到的消息;
    @FXML
    private TextField kafka消费者主题;
    private volatile boolean stop;
    @FXML
    private TextField kafka键;
    @FXML
    private TextField kafka生产者主题;
    @FXML
    private TextArea kafka值;
    @FXML
    private TextField kafka客户端端口;
    @FXML
    private TextArea 发送状态;
    @FXML
    private TextField 特定组;
    @FXML
    private TextField kafka集群IP;
    @FXML
    private TextField kafka客户端IP;
    private Properties producerProperties;
    private Properties consumerProperties;
    @FXML
    private TextField kafka客户端用户名;
    @FXML
    private TextField kafka客户端密码;
    @FXML
    private TextField kafka客户端命令行位置;


    @FXML
    void 保存(ActionEvent event) {
        kafkaConfig.setClustip(kafka集群IP.getText());
        kafkaConfig.setClientPath(kafka客户端命令行位置.getText());
        kafkaConfig.setIp(kafka客户端IP.getText());
        kafkaConfig.setPort(kafka客户端端口.getText());
        kafkaConfig.setRootPassword(kafka客户端密码.getText());
        kafkaConfig.setRootUsername(kafka客户端用户名.getText());
        kafkaConfig.setGroup(特定组.getText());
        FileUtil.writeObject(kafkaConfigFile, kafkaConfig);
    }

    @FXML
    void 清空(ActionEvent event) {
        收到的消息.clear();
        consumerQueue.clear();
    }

    @FXML
    void 开始监听(ActionEvent event) {
        if (StringUtils.isEmpty(kafka消费者主题.getText())) {
            DialogUtils.AlertInfomation("kafka消费者主题 不能为空");
            return;
        }
        List<String> topicList =
                Arrays.stream(kafka消费者主题.getText().split(";")).collect(Collectors.toList());
        stop = true;
        executorService.execute(() -> {
            stop = false;
            if (kafkaConsumer == null)
                kafkaConsumer = new KafkaConsumer<String, String>(consumerProperties);
            kafkaConsumer.subscribe(topicList);
            while (!stop) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    String content = "收到消息 topic = " + record.topic() + " key = " + record.key() +
                            " val = " + record.value();
                    DialogUtils.showContent(content, 收到的消息, consumerQueue);
                    log.info("收到消息:{}", content);
                }
            }
            kafkaConsumer.close();
            kafkaConsumer = null;
        });
    }

    @SneakyThrows
    @Override
    public void initController() {
        String dir = System.getProperty("user.dir");
        kafkaConfigFile = new File(dir + "/" + "kafkaConfig.json");
        kafkaConfig = FileUtil.readObject(kafkaConfigFile, KafkaConfig.class);
        kafka集群IP.setText(kafkaConfig.getClustip());
        kafka客户端IP.setText(kafkaConfig.getIp());
        kafka客户端命令行位置.setText(kafkaConfig.getClientPath());
        kafka客户端端口.setText(kafkaConfig.getPort());
        kafka客户端密码.setText(kafkaConfig.getRootPassword());
        kafka客户端用户名.setText(kafkaConfig.getRootUsername());
        特定组.setText(kafkaConfig.getGroup());
        consumerProperties = new Properties();
        consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,
                "myjavafx" + Inet4Address.getLocalHost().getHostName());
        consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                mainApp.rootController.getContext().getKafkaServers());

        producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.setProperty(
                CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                mainApp.rootController.getContext().getKafkaServers());
        kafkaProducer = new KafkaProducer<>(producerProperties);
        dialogStage.setOnCloseRequest((event) -> {
            stop = true;
            kafkaProducer.close();
            kafkaProducer = null;
            executorService.shutdown();
        });
    }

    @SneakyThrows
    @FXML
    void 发送消息(ActionEvent event) {

        String key = (kafka键.getText() == null || kafka键.getText().length() == 0) ? null :
                kafka键.getText();
        Future<RecordMetadata> future = kafkaProducer
                .send(new ProducerRecord<String, String>(kafka生产者主题.getText(), key,
                        kafka值.getText()));
        RecordMetadata recordMetadata = future.get(1, TimeUnit.SECONDS);
        DialogUtils.showContent(
                "是否成功" + recordMetadata.hasOffset() + " topic=" + recordMetadata.topic() +
                        "patition=" + recordMetadata.partition() + "offset=" +
                        recordMetadata.offset(), 发送状态, producerQueue);

    }

    @FXML
    void 停止监听(ActionEvent event) {
        stop = true;
    }

    @FXML
    void 查看所有消费组(ActionEvent event) {
        RootController rootController = getMainApp().getRootController();
        ServerConfig serverConfig = new MyServerConfig().setRootUsername(kafka客户端用户名.getText())
                .setRootPassword(kafka客户端密码.getText()).setIp(kafka客户端IP.getText())
                .setPort(Integer.valueOf(kafka客户端端口.getText()));
        String client = kafka客户端命令行位置.getText() + "/kafka-consumer-groups.sh";
        String replaceAll = client.replaceAll("//", "/");
        mainApp.openEditDialogForResult("查看所有消费组", "Content.fxml", JschUtil.getExecResult(serverConfig,
                replaceAll + " --bootstrap-server " + kafka集群IP.getText() + " --list"));
    }

    @FXML
    void 查看特定组消费情况(ActionEvent event) {
        RootController rootController = getMainApp().getRootController();
        ServerConfig serverConfig = new ServerConfig().setRootUsername(kafka客户端用户名.getText())
                .setRootPassword(kafka客户端密码.getText()).setIp(kafka客户端IP.getText())
                .setPort(Integer.valueOf(kafka客户端端口.getText()));
        String client = kafka客户端命令行位置.getText() + "/kafka-consumer-groups.sh";
        String replaceAll = client.replaceAll("//", "/");
        String execResult = JschUtil.getExecResult(serverConfig,
                replaceAll + " --bootstrap-server " + kafka集群IP.getText() + " --describe --group " +
                        特定组.getText());
        mainApp.openEditDialogForResult("查看消费情况", "Content.fxml", execResult);

    }

    @FXML
    void 查看所有topic(ActionEvent event) {
        if (!checkInput()) {
            return;
        }
        if (kafkaConsumer == null)
            kafkaConsumer = new KafkaConsumer<String, String>(consumerProperties);
        Map<String, List<PartitionInfo>> listTopics = kafkaConsumer.listTopics();
        Joiner joiner = Joiner.on(System.lineSeparator());
        String join = joiner.join(listTopics.keySet());
        mainApp.openEditDialogForResult("查看所有topic", "Content.fxml", join);

    }

    private boolean checkInput() {
        if (StringUtils.isEmpty(kafka客户端IP.getText()) ||
                StringUtils.isEmpty(kafka集群IP.getText()) ||
                StringUtils.isEmpty(kafka客户端命令行位置.getText()) ||
                StringUtils.isEmpty(kafka客户端密码.getText()) ||
                StringUtils.isEmpty(kafka客户端用户名.getText())) {
            DialogUtils.AlertInfomation("输入不完整!");
            return false;
        }
        return true;
    }

}
