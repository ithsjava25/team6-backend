package org.example.team6backend.page;

import org.example.team6backend.incident.dto.IncidentRequest;
import org.example.team6backend.incident.entity.Incident;
import org.example.team6backend.incident.service.IncidentService;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
	private final UserService userService;
    private final IncidentService incidentService;

    public PageController(UserService userService, IncidentService incidentService) {
		this.userService = userService;
        this.incidentService = incidentService;
    }

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model, Pageable pageable) {

        if (userDetails != null && userDetails.getUser() != null) {
            AppUser user = userDetails.getUser();
            model.addAttribute("user", userDetails.getUser());
            model.addAttribute("role", user.getRole().name());

            Page<Incident> incidents;

            switch (user.getRole()) {
                case RESIDENT -> incidents = incidentService.findByCreatedBy(user, pageable);
                case HANDLER -> incidents = incidentService.findByAssignedTo(user, pageable);
                case ADMIN -> incidents = incidentService.findAll(pageable);
                default -> incidents = Page.empty();
            }
            model.addAttribute("incidents", incidents);

        } else {
            model.addAttribute("role", "PENDING");
        }
		return "dashboard";
	}

	@GetMapping("/incidents")
	public String incidents(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		model.addAttribute("role", userDetails.getUser().getRole().name());
		return "incidents";
	}

	@GetMapping("/create-incident")
	public String createIncident(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
                                 HttpServletRequest request) {
		AppUser user = userDetails.getUser();
		String role = user.getRole().name();

        CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrf);

		if (role.equals("RESIDENT") || role.equals("ADMIN")) {
			model.addAttribute("role", role);
			model.addAttribute("user", user);
            model.addAttribute("incidentRequest", new IncidentRequest());
			return "createincident";
		}
		return "redirect:/dashboard";
	}

    @PostMapping("/create-incident")
    public String submitIncident(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @ModelAttribute IncidentRequest incidentRequest, Model model) {
        AppUser user = userDetails.getUser();
        String role = user.getRole().name();

        Incident incident = new Incident();
        incident.setSubject(incidentRequest.getSubject());
        incident.setDescription(incidentRequest.getDescription());
        incident.setIncidentCategory(incidentRequest.getIncidentCategory());
        incident.setCreatedBy(user);

        Incident saved = incidentService.createIncident(incident);

        model.addAttribute("success", "Incident created successfully!");
        model.addAttribute("incidentRequest", incidentRequest);
        return "redirect:/incident/" + saved.getId();
    }

    @GetMapping("/incident/{id}")
    public String viewIncident(@PathVariable Long id, Model model) {
        Incident incident = incidentService.findById(id);

        if (incident == null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("incident", incident);
        return "view-incident";
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

	@GetMapping("/demo")
	public String demo() {
		return "demo";
	}
}
