
package asl.tcpproxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class AppConfig {

    List<String> clientWhiteList = new ArrayList<String>();

    List<String> tunnels = new ArrayList<String>();

    int connectTimeout;

    Optional<BuildProperties> buildProperties;

    public AppConfig(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    public InetAddress[] getClientWhiteListAddresses() {
        try {

            List<InetAddress> addressList = new ArrayList<>();
            for (String addressString : getClientWhiteList()) {
                if ("*".equals(addressString.trim())) {
                    return null;
                } else {

                    addressList.add(InetAddress.getByName(addressString));

                }
            }
            if (addressList.size() > 0) {
                return addressList.toArray(new InetAddress[addressList.size()]);
            } else {
                return null;
            }

        } catch (UnknownHostException e) {
            throw new IllegalStateException(
                    String.format("Unable to proceed. Wrong white list address: %s", e.toString()),
                    e);
        }
    }

    public List<String> getClientWhiteList() {
        return clientWhiteList;
    }

    public void setClientWhiteList(List<String> clientWhiteList) {
        this.clientWhiteList = clientWhiteList;
    }

    public List<String> getTunnels() {
        return tunnels;
    }

    public void setTunnels(List<String> tunnels) {
        this.tunnels = tunnels;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String appVersion() {
        return buildProperties.map(v -> v.getVersion()).orElse("dev version");
    }

}
