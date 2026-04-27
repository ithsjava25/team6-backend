package org.example.team6backend.dev;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.team6backend.security.CustomUserDetails;
import org.example.team6backend.user.entity.AppUser;
import org.example.team6backend.user.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/dev")
public class DevAuthController {

	private final AppUserRepository userRepository;

	@Value("${dev.mode.enabled:false}")
	private boolean devModeEnabled;

	public DevAuthController(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/enabled")
	@ResponseBody
	public Map<String, Boolean> isDevModeEnabled() {
		Map<String, Boolean> response = new HashMap<>();
		response.put("enabled", devModeEnabled);
		return response;
	}

	@GetMapping("/test")
	public void test(HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.getWriter().println("<h1>Dev endpoint works</h1><p>Dev mode enabled: " + devModeEnabled + "</p>");
	}

	@GetMapping("/all-users")
	public void getAllUsers(HttpServletResponse response) throws IOException {
		if (!devModeEnabled) {
			response.sendError(403, "Dev mode disabled");
			return;
		}

		var users = userRepository.findAll();
		response.setContentType("application/json");
		var writer = response.getWriter();

		writer.print("[");
		for (int i = 0; i < users.size(); i++) {
			var user = users.get(i);
			writer.print(String.format("""
					    {
					        "githubLogin": "%s",
					        "name": "%s",
					        "email": "%s",
					        "role": "%s",
					        "active": %s,
					        "id": "%s"
					    }
					""", escapeJson(user.getGithubLogin()), escapeJson(user.getName()),
					escapeJson(user.getEmail() != null ? user.getEmail() : ""), user.getRole().name(), user.isActive(),
					user.getId()));
			if (i < users.size() - 1)
				writer.print(",");
		}
		writer.print("]");
	}

	@GetMapping("/users")
	public void listUsers(HttpServletResponse response) throws IOException {
		if (!devModeEnabled) {
			response.sendError(403, "Dev mode disabled - Enable with --spring.profiles.active=dev");
			return;
		}

		var users = userRepository.findAll();
		response.setContentType("text/html");
		var writer = response.getWriter();
		writer.println("""
				    <!DOCTYPE html>
				    <html>
				    <head>
				        <meta charset="UTF-8">
				        <title>Dev Mode - User Switcher</title>
				        <style>
				            * { margin: 0; padding: 0; box-sizing: border-box; }
				            body {
				                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace;
				                background: #000000;
				                color: #ffffff;
				                padding: 2rem;
				            }
				            .container { max-width: 800px; margin: 0 auto; }
				            h1 { color: #8b5cf6; margin-bottom: 0.5rem; }
				            p { color: #6b7280; margin-bottom: 2rem; }
				            .dev-badge {
				                background: #8b5cf6;
				                color: white;
				                padding: 0.25rem 0.75rem;
				                border-radius: 9999px;
				                font-size: 0.75rem;
				                display: inline-block;
				                margin-left: 0.5rem;
				                vertical-align: middle;
				            }
				            .user-card {
				                background: #0a0a0a;
				                border: 1px solid #1a1a1a;
				                border-radius: 1rem;
				                padding: 1rem;
				                margin-bottom: 0.75rem;
				                transition: all 0.2s;
				            }
				            .user-card:hover {
				                border-color: #8b5cf6;
				                transform: translateX(4px);
				            }
				            .user-link {
				                text-decoration: none;
				                display: flex;
				                justify-content: space-between;
				                align-items: center;
				                flex-wrap: wrap;
				                gap: 0.5rem;
				            }
				            .user-info {
				                display: flex;
				                align-items: center;
				                gap: 1rem;
				                flex-wrap: wrap;
				            }
				            .user-name {
				                color: #ffffff;
				                font-weight: 600;
				                font-size: 1rem;
				            }
				            .user-role {
				                display: inline-block;
				                padding: 0.25rem 0.5rem;
				                border-radius: 9999px;
				                font-size: 0.7rem;
				                font-weight: 500;
				            }
				            .role-PENDING { background: #6b7280; color: white; }
				            .role-RESIDENT { background: #10b981; color: white; }
				            .role-HANDLER { background: #f97316; color: white; }
				            .role-ADMIN { background: #ef4444; color: white; }
				            .user-email {
				                color: #6b7280;
				                font-size: 0.75rem;
				            }
				            .status-badge {
				                padding: 0.25rem 0.5rem;
				                border-radius: 9999px;
				                font-size: 0.7rem;
				            }
				            .status-active { background: #10b981; color: white; }
				            .status-inactive { background: #ef4444; color: white; }
				            .inactive-user {
				                opacity: 0.6;
				            }
				            .back-link {
				                display: inline-block;
				                margin-top: 2rem;
				                color: #6b7280;
				                text-decoration: none;
				                padding: 0.5rem 1rem;
				                border: 1px solid #2a2a2a;
				                border-radius: 9999px;
				                transition: all 0.2s;
				            }
				            .back-link:hover {
				                border-color: #8b5cf6;
				                color: #8b5cf6;
				            }
				            hr {
				                border-color: #1a1a1a;
				                margin: 2rem 0;
				            }
				        </style>
				    </head>
				    <body>
				        <div class="container">
				            <h1>Developer Mode <span class="dev-badge">Active</span></h1>
				            <p>Test different roles in dev mode</p>
				""");

		for (var user : users) {
			String inactiveClass = !user.isActive() ? "inactive-user" : "";
			String statusClass = user.isActive() ? "status-active" : "status-inactive";
			String statusText = user.isActive() ? "Active" : "Inactive";

			writer.printf("""
					<div class="user-card %s">
					    <a href="/dev/switch-user?githubLogin=%s" class="user-link">
					        <div class="user-info">
					            <span class="user-name">%s</span>
					            <span class="user-role role-%s">%s</span>
					            <span class="user-email">%s</span>
					        </div>
					        <span class="status-badge %s">%s</span>
					    </a>
					</div>
					""", inactiveClass, user.getGithubLogin(), escapeHtml(user.getName()), user.getRole().name(),
					user.getRole().name(), escapeHtml(user.getEmail() != null ? user.getEmail() : "no email"),
					statusClass, statusText);
		}

		writer.println("""
				            <hr>
				            <a href="/dev/all-users" class="back-link" style="margin-left: 0.5rem;">View JSON</a>
				        </div>
				    </body>
				    </html>
				""");
	}

	@GetMapping("/switch-user")
	public void switchUser(@RequestParam String githubLogin, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (!devModeEnabled) {
			response.sendError(403, "Dev mode is disabled");
			return;
		}

		AppUser user = userRepository.findByGithubLogin(githubLogin)
				.orElseThrow(() -> new RuntimeException("User not found: " + githubLogin));

		if (!user.isActive()) {
			response.setContentType("text/html");
			response.getWriter().println(
					"""
							    <!DOCTYPE html>
							    <html>
							    <head>
							        <meta charset="UTF-8">
							        <title>Account Inactive</title>
							        <style>
							            body { font-family: monospace; background: #000; color: #fff; text-align: center; padding: 2rem; }
							            h1 { color: #ef4444; }
							            a { color: #8b5cf6; text-decoration: none; }
							            .back-link { display: inline-block; margin-top: 1rem; padding: 0.5rem 1rem; border: 1px solid #2a2a2a; border-radius: 9999px; }
							            .back-link:hover { border-color: #8b5cf6; }
							        </style>
							    </head>
							    <body>
							        <h1>⚠ Account Inactive</h1>
							        <p>This account has been deactivated.</p>
							        <p>You cannot switch to an inactive user.</p>
							        <a href="/dev/users" class="back-link">← Back to user list</a>
							    </body>
							    </html>
							""");
			return;
		}

		Map<String, Object> attributes = Map.of("id", user.getGithubId(), "login", user.getGithubLogin(), "email",
				user.getEmail() != null ? user.getEmail() : "", "name", user.getName(), "avatar_url",
				user.getAvatarUrl() != null ? user.getAvatarUrl() : "");

		CustomUserDetails customUserDetails = new CustomUserDetails(user, attributes);

		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(customUserDetails,
				customUserDetails.getAuthorities(), "github");

		SecurityContextHolder.getContext().setAuthentication(authentication);

		HttpSession session = request.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext());

		response.sendRedirect("/dashboard.html");
	}

	private String escapeJson(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private String escapeHtml(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&#39;");
	}
}
