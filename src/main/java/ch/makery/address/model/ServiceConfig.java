package ch.makery.address.model;

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
    private String displayName = "默认名称";
    private String loggzPatten;
    private String logPath;
    private String redefinePath;
    private String serviceName;
    private String remoteLibDir;
    private String startCmd;
    private String stopCmd;
    private String localFiles;

    @Override
    public String toString() {
        return displayName;
    }
}