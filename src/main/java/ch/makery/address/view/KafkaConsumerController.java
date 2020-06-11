package ch.makery.address.view;

import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.google.common.collect.EvictingQueue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
@Slf4j
public class KafkaConsumerController extends DialogController {
    KafkaConsumer<String,String> kafkaConsumer;
    EvictingQueue<Object> queue= EvictingQueue.create(50);
    @FXML
    private TextArea 收到的消息;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    @FXML
    private TextField kafka主题;
  private volatile boolean stop;
    @FXML
    void 开始监听(ActionEvent event) {
        if(StringUtils.isEmpty(kafka主题.getText())){
            DialogUtils.AlertInfomation("kafka主题 不能为空");
            return;
        }
        List<String> topicList =
                Arrays.stream(kafka主题.getText().split(";")).collect(Collectors.toList());
        stop=true;
        executorService.execute(()->{
            stop=false;
            kafkaConsumer.subscribe(topicList);
            while (!stop){
                ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    String content="收到消息 topic = "+record.topic()+" key = "+record.key()+" val = "+record.value();
                    DialogUtils.showContent(content,收到的消息,queue);
                    log.info("收到消息:{}",content);
                }
            }
            kafkaConsumer.close();
        });
    }

    @Override
    public void initController() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");

        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"myjavafx");
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,mainApp.rootController.getContext().getKafkaServers());
       kafkaConsumer = new KafkaConsumer<String,String>(properties);
        dialogStage.setOnCloseRequest((event)->{
            stop=true;
        });
    }
}
