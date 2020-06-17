package ch.makery.address.model;

import javafx.beans.property.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
* Model class for a Person.
*
* @author Marco Jakob
*/
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ServerConfig {

    private String ip;
    private Integer port;
    private String 别名;
    private String serviceUsername;
    private String rootUsername;
    private String rootPassword;
    private SimpleBooleanProperty selected=new SimpleBooleanProperty();


    @Override
    public String toString() {
        return StringUtils.isEmpty(别名)?ip:别名;
    }

}