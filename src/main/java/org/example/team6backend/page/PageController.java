package org.example.team6backend.page;

import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	private final UserService userService;

	public PageController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		AppUser user = userDetails.getUser();
		model.addAttribute("user", user);
		model.addAttribute("role", user.getRole().name());
		return "dashboard";
	}

	@GetMapping("/incidents")
	public String incidents(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		model.addAttribute("role", userDetails.getUser().getRole().name());
		return "incidents";
	}

	@GetMapping("/create-incident")
	public String createIncident(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		AppUser user = userDetails.getUser();

		String role = user.getRole().name();
		if (role.equals("RESIDENT") || role.equals("ADMIN")) {
			model.addAttribute("role", role);
			model.addAttribute("user", user);
			return "createincident";
		}
		return "redirect:/dashboard";
	}

	@GetMapping("/profile")
	public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		AppUser user = userDetails.getUser();
		model.addAttribute("user", user);
		model.addAttribute("role", user.getRole().name());
		return "profile";
	}

	@GetMapping("/admin")
	public String admin(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		AppUser user = userDetails.getUser();

		if (user.getRole().name().equals("ADMIN")) {
			model.addAttribute("user", user);
			model.addAttribute("role", user.getRole().name());
			return "admin";
		}

		return "redirect:/dashboard";
	}

	@GetMapping("/incidents/{id}")
	public String viewIncident(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		model.addAttribute("role", userDetails.getUser().getRole().name());
		return "viewincident";
	}

	@GetMapping("/demo")
	public String demo() {
		return "demo";
	}
}
