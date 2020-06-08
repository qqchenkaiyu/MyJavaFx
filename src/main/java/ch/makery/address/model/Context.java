package ch.makery.address.model;

import lombok.Data;

@Data
public class Context {
    //文本编辑器
    private String textEditor="notepad";
    //覆盖率路径
    private String coveragePath;
}
