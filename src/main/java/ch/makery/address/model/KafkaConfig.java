package ch.makery.address.model;

import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
* Model class for a Person.
*
* @author Marco Jakob
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KafkaConfig {
    private String clustip;
    private String ip;
    private String port;
    private String rootUsername;
    private String rootPassword;
    private String clientPath;
    private String group;
    @Override
    public String toString() {
        return ip;
    }

}