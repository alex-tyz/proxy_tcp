
package asl.tcpproxy.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import asl.tcpproxy.AppConfig;
import asl.tcpproxy.tunnels.Tunnel;

@Service
public class TunnelsService {

	AppConfig config;

	List<Tunnel> tunnels;

	public TunnelsService(AppConfig config) {
        this.config = config;
    }

    @PostConstruct
	private void init() {
		tunnels = config.getTunnels().parallelStream().map(tunnelDesc -> createTunnel(tunnelDesc)).collect(Collectors.toList());
	}
    
    @PreDestroy
    private void close() {
        tunnels.forEach(tunnel -> tunnel.close());
    }
    
    public Map<String, String> status() {
        return tunnels.stream().collect(Collectors.toMap(t -> t.descriptor(), t -> t.toString()));
    }

	private Tunnel createTunnel(String tunnelDesc) {
		Tunnel tunnel = new Tunnel(tunnelDesc, config.getConnectTimeout(), config.getClientWhiteListAddresses());

		tunnel.open();

		return tunnel;
	}
}
