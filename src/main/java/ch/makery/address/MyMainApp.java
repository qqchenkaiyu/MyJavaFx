package ch.makery.address;

import ch.makery.address.anotation.DefaultPackage;
import ch.makery.address.anotation.DefaultView;
import ch.makery.address.util.MainApp;
import lombok.Data;

/**
 *
 */
@DefaultView("ServiceOverview.fxml")
@DefaultPackage("view")
@Data
public class MyMainApp extends MainApp {
    public MyMainApp() {
        System.out.println("我被创建了 " + this);
    }
}