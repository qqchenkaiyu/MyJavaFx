/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package ch.makery.address.util;

import javafx.stage.Stage;
import lombok.Data;

// 

/**
 * 用于显示一个对话框 因为编辑对话框由子类实现，简单对话框由工具类实现所以不怎么使用
 */
@Data
public abstract class DialogController extends Controller {
    public Stage dialogStage;
    public boolean okClicked;
}

