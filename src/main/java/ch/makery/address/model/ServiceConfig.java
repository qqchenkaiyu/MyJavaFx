package ch.makery.address.model;

import javafx.beans.property.BooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
* Model class for a Person.
*
* @author Marco Jakob
*/
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConfig {
    private String coveragePath;
    private String logPath;
    private String serviceName;
    private String remoteLibDir;
    private String localFiles;
    @Override
    public String toString() {
        return serviceName;
    }
}