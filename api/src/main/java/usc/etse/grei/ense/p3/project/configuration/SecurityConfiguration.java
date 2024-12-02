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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import usc.etse.grei.ense.p3.project.filter.AuthenticationFilter;
import usc.etse.grei.ense.p3.project.filter.AuthorizationFilter;
import usc.etse.grei.ense.p3.project.service.AuthenticationService;

import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase de configuración de seguridad
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfiguration {

	private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	private final AuthenticationService auth;

	public SecurityConfiguration(AuthenticationService auth) {
		this.auth = auth;
	}

	/**
	 * Metodo que crea un AuthenticationManager para autenticar a los usuarios
	 *
	 * @return autenticador de usuarios
	 */
	@Bean
	public AuthenticationManager authManager() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(auth);
		provider.setPasswordEncoder(passwordEncoder());

		return provider::authenticate;

	}

	/**
	 * Metodo que configura la cadena de filtros de seguridad para las solicitudes HTTP
	 *
	 * @param http        configuración de la seguridad HTTP
	 * @param authManager autenticador de usuarios
	 * @return filtros de seguridad HTTP
	 * @throws Exception excepcion
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
				.addFilterBefore(new AuthenticationFilter(authManager, tokenSignKey()), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new AuthorizationFilter(authManager, tokenSignKey()), UsernamePasswordAuthenticationFilter.class)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();

	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("http://localhost:3000");
		config.addAllowedHeader("*");
		config.addExposedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	/**
	 * Metodo que define el algoritmo de hashing para las contraseñas de los usurios
	 *
	 * @return algoritmo de hashing
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Metodo que define la jerarquía de roles de la aplicación
	 *
	 * @return jerarquía de roles
	 */
	@Bean
	public RoleHierarchy roleHierarchy() {

		Map<String, List<String>> roles = new HashMap<>();
		roles.put("ROLE_ADMIN", Collections.singletonList("ROLE_USER"));

		// Genera la cadena de jerarquía a partir del mapa
		String hierarchyString = RoleHierarchyUtils.roleHierarchyFromMap(roles);

		// Usa fromHierarchy para crear RoleHierarchyImpl
		return RoleHierarchyImpl.fromHierarchy(hierarchyString);

	}

	/**
	 * Metodo que proporciona una clave de firma para los tokens
	 *
	 * @return clave de firma
	 */
	@Bean
	public Key tokenSignKey() {
		return SecurityConfiguration.key;
	}

}