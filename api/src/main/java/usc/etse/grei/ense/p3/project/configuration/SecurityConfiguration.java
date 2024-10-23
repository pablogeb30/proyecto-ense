package usc.etse.grei.ense.p3.project.configuration;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import usc.etse.grei.ense.p3.project.filter.AuthenticationFilter;
import usc.etse.grei.ense.p3.project.filter.AuthorizationFilter;
import usc.etse.grei.ense.p3.project.service.AuthenticationService;

import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	private final AuthenticationService auth;

	public SecurityConfiguration(AuthenticationService auth) {
		this.auth = auth;
	}

	@Bean
	public AuthenticationManager authManager() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(auth);
		provider.setPasswordEncoder(passwordEncoder());

		return provider::authenticate;

	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
				.addFilterBefore(new AuthenticationFilter(authManager, tokenSignKey()), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new AuthorizationFilter(authManager, tokenSignKey()), UsernamePasswordAuthenticationFilter.class)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public RoleHierarchy roleHierarchy() {

		Map<String, List<String>> roles = new HashMap<>();
		roles.put("ROLE_ADMIN", Collections.singletonList("ROLE_USER"));

		RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
		hierarchy.setHierarchy(RoleHierarchyUtils.roleHierarchyFromMap(roles));

		return hierarchy;

	}

	@Bean
	public Key tokenSignKey() {
		return SecurityConfiguration.key;
	}

}