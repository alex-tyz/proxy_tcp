
package asl.tcpproxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TcpProxyApplication {

	@Autowired
	AppConfig config;
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TcpProxyApplication.class);
		app.run();
	}
}
