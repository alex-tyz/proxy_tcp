
package asl.tcpproxy.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import asl.tcpproxy.AppConfig;

@Service
public class StatusService {

    AppConfig config;

    TunnelsService tunnelsService;

    public StatusService(AppConfig config, TunnelsService tunnelsService) {
        this.config = config;
        this.tunnelsService = tunnelsService;
    }

    public String status() {
        return "TCP Proxy up and running";
    }
    
    public int timeout() {
        return config.getConnectTimeout();
    }

    public List<String> clientWhiteList() {
        return config.getClientWhiteList();
    }
    
    public Map<String, String> tunnels() {
        return tunnelsService.status();
    }
    
    public String appVersion() {
        return config.appVersion();
    }

    public Map<String, Object> configurationInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("status", status());
        info.put("Client White List", clientWhiteList());
        info.put("Connect timeout", timeout());

        info.put("tunnels: ", tunnelsService.status());

        return info;
    }
}
