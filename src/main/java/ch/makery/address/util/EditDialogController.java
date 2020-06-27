/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package ch.makery.address.util;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.util.HashMap;
import java.util.Map;

// Referenced classes of package cky.fxUtil:
//            Controller
@Data
public abstract class EditDialogController<T> extends DialogController {
    public T obj;
    public Map<String, String> propMap;
    public Map<TextField, String> fieldMap = new HashMap<TextField, String>();

    public void setObject(T t) {
        this.obj = t;
        try {
            propMap = BeanUtils.describe(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void 取消() {
        dialogStage.close();
    }

    @FXML
    private void 确定() {
        if (isInputValid()) {
            save();
            okClicked = true;
            dialogStage.close();
        }
    }

    public abstract boolean isInputValid();

    public abstract void save();
}
/*
	DECOMPILATION REPORT

	Decompiled from: C:\Workspaces\com.connor.unv.plm2\lib\MyFXUtil.jar
	Total time: 12 ms
	Jad reported messages/errors:
	Exit status: 0
	Caught exceptions:
*/