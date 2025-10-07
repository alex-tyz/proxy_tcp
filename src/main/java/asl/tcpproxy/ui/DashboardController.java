package asl.tcpproxy.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import asl.tcpproxy.services.StatusService;

@Controller
public class DashboardController {

	StatusService statusService;

	public DashboardController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/")
	public String home(Model model) {
        
        model.addAttribute("status", statusService.status());
        model.addAttribute("timeout", statusService.timeout());
        model.addAttribute("appVersion", statusService.appVersion());
        model.addAttribute("tunnels", statusService.tunnels());
        
        return "index";
	}
}
