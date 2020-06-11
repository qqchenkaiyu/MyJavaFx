package ch.makery.address.view;

import ch.makery.address.util.DialogController;
import ch.makery.address.util.DialogUtils;
import com.google.common.collect.EvictingQueue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;
public class KafkaProducerController extends DialogController {
    KafkaProducer<String, String> kafkaProducer;
    EvictingQueue<Object> queue= EvictingQueue.create(50);
    StringBuffer stringBuffer=new StringBuffer();
    @FXML
    private TextField kafka键;

    @FXML
    private TextField kafka主题;

    @FXML
    private TextArea kafka值;

    @FXML
    private TextArea 发送状态;

    @SneakyThrows
    @FXML
    void 发送消息(ActionEvent event) {

        String key = (kafka键.getText() == null || kafka键.getText().length() == 0) ? null :
                kafka键.getText();
        Future<RecordMetadata> future = kafkaProducer
                .send(new ProducerRecord<String, String>(kafka主题.getText(), key, kafka值.getText()));
        RecordMetadata recordMetadata = future.get();
        DialogUtils.showContent("是否成功" + recordMetadata.hasOffset() + " topic=" + recordMetadata.topic() +
                "patition=" + recordMetadata.partition() + "offset=" + recordMetadata.offset(),发送状态,queue);

    }

    @Override
    public void initController() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,mainApp.rootController.getContext().getKafkaServers());
        kafkaProducer = new KafkaProducer<>(properties);
        dialogStage.setOnCloseRequest((event)->{
            kafkaProducer.close();
        });
    }

}