/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package ch.makery.address.util;

import lombok.Data;

// initController方法将组件属性的值都赋值
@Data
public abstract class Controller {
    public MainApp mainApp;

    public abstract void initController();
}