package ch.makery.address.model;

import javafx.beans.property.*;
import lombok.*;

/**
* Model class for a Person.
*
* @author Marco Jakob
*/
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerConfig {

    private String ip;
    private Integer port;
    private String serviceUsername;
    private String servicePassword;
    private String rootUsername;
    private String rootPassword;
private SimpleBooleanProperty selected=new SimpleBooleanProperty();


    @Override
    public String toString() {
        return ip;
    }
    //    public  <T> ObservableValue<Boolean> isSelected(T t) {
//        ServerConfig tt= (ServerConfig) t;
//        return tt.selected;
//    }
    //private boolean selected;

}