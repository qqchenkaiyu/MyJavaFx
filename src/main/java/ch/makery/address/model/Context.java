package ch.makery.address.model;

import ch.makery.address.anotation.ChineseName;
import lombok.Data;

@Data
public class Context {
    //文本编辑器
    @ChineseName("文本编辑器")
    private String textEditor = "notepad";
    //覆盖率路径
    @ChineseName("覆盖率路径")
    private String coveragePath = "";
    //覆盖率命令
    @ChineseName("覆盖率命令")
    private String coverageCmd = "";
    @ChineseName("kafka集群位置")
    private String kafkaServers = "192.168.99.100:9092";
    @ChineseName("kafka客户端位置")
    private String kafkaBin = "";
    @ChineseName("重定义地址")
    private String redefinePath = "F://test";
    private String LatestClass = "com.hello";
}
