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

import java.io.IOException;
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
					        "active": %s
					    }
					""", escapeJson(user.getGithubLogin()), escapeJson(user.getName()),
					escapeJson(user.getEmail() != null ? user.getEmail() : ""), user.getRole().name(),
					user.isActive()));
			if (i < users.size() - 1)
				writer.print(",");
		}
		writer.print("]");
	}

	private String escapeJson(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
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
							        <title>Account Inactive</title>
							        <style>
							            body { font-family: monospace; background: #000; color: #fff; text-align: center; padding: 2rem; }
							            h1 { color: #ef4444; }
							            a { color: #10b981; text-decoration: none; }
							            a:hover { text-decoration: underline; }
							        </style>
							    </head>
							    <body>
							        <h1>⚠Account Inactive</h1>
							        <p>This account has been deactivated.</p>
							        <p>You cannot switch to an inactive user.</p>
							        <a href="/dev/users">← Back to user list</a>
							    </body>
							    </html>
							""");
			return;
		}

		Map<String, Object> attributes = Map.of("id", user.getGithubId(), "login", user.getGithubLogin(), "email",
				user.getEmail(), "name", user.getName(), "avatar_url", user.getAvatarUrl());

		CustomUserDetails customUserDetails = new CustomUserDetails(user, attributes);

		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(customUserDetails,
				customUserDetails.getAuthorities(), "github");

		SecurityContextHolder.getContext().setAuthentication(authentication);

		HttpSession session = request.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext());

		response.sendRedirect("/dashboard");
	}

	@GetMapping("/users")
	public void listUsers(HttpServletResponse response) throws IOException {
		if (!devModeEnabled) {
			response.sendError(403, "Dev mode disabled");
			return;
		}

		var users = userRepository.findAll();
		response.setContentType("text/html");
		var writer = response.getWriter();
		writer.println("""
				    <!DOCTYPE html>
				    <html>
				    <head>
				        <title>Switch User (Dev Mode)</title>
				        <style>
				            body { font-family: monospace; background: #000; color: #fff; padding: 2rem; }
				            a { color: #10b981; display: block; margin: 0.5rem 0; text-decoration: none; }
				            a:hover { text-decoration: underline; }
				            .role { font-size: 0.8rem; color: #6b7280; }
				            .inactive { color: #ef4444; text-decoration: line-through; }
				        </style>
				    </head>
				    <body>
				        <h1>Switch User (Development Only)</h1>
				        <p>Click any user to switch to their account:</p>
				""");

		for (var user : users) {
			String inactiveClass = !user.isActive() ? "inactive" : "";
			writer.printf("""
					    <div>
					        <a href="/dev/switch-user?githubLogin=%s" class="%s">
					            <strong>%s</strong>
					            <span class="role">[%s]</span>
					            <span style="font-size:0.7rem;color:#6b7280;"> (%s)</span>
					            %s
					        </a>
					    </div>
					""", user.getGithubLogin(), inactiveClass, user.getName(), user.getRole(), user.getEmail(),
					!user.isActive() ? " INACTIVE" : "");
		}

		writer.println("""
				    </body></html>
				""");
	}
}
