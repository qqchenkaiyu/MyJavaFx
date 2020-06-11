package ch.makery.address.model;

import lombok.Data;

@Data
public class Context {
    //文本编辑器
    private String textEditor="notepad";
    //覆盖率路径
    private String coveragePath="";
    //覆盖率命令
    private String coverageCmd="";
    //kafka集群位置
    private String kafkaServers="192.168.99.100:9092";
}
