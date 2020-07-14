package ch.makery.address.model;

import com.cky.jsch.ServerConfig;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
public class MyServerConfig extends ServerConfig  {

    private SimpleBooleanProperty selected = new SimpleBooleanProperty();

    @Override
    public String toString() {
        return StringUtils.isEmpty(alias) ? ip : alias;
    }

}