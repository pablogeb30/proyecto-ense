package usc.etse.grei.ense.p3.project.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro de autenticación de usuarios
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static long TOKEN_DURATION = Duration.ofMinutes(60).toMillis();
	private final AuthenticationManager manager;
	private final Key key;

	public AuthenticationFilter(AuthenticationManager manager, Key key) {
		this.manager = manager;
		this.key = key;
	}

	/**
	 * Metodo que autentica al usuario comprobando sus credenciales de inicio de sesión
	 *
	 * @param request solicitud HTTP
	 * @param response respuesta HTTP
	 * @return detalles de la autenticación
	 * @throws AuthenticationException excepcion
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		try {

			JsonNode credentials = new ObjectMapper().readValue(request.getInputStream(), JsonNode.class);

			return manager.authenticate(new UsernamePasswordAuthenticationToken(credentials.get("email").textValue(), credentials.get("password").textValue()));

		} catch (IOException ex) {

			throw new RuntimeException(ex);

		}

	}

	/**
	 * Metodo que genera un token cuando la autenticación es exitosa
	 *
	 * @param request solicitud HTTP
	 * @param response respuesta HTTP
	 * @param chain filtro de la cadena de seguridad
	 * @param authResult detalles de la autenticación
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {

		long now = System.currentTimeMillis();

		List<String> authorities = authResult.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

		JwtBuilder tokenBuilder = Jwts.builder().setSubject(((User) authResult.getPrincipal()).getUsername()).setIssuedAt(new Date(now)).setExpiration(new Date(now + TOKEN_DURATION)).claim("roles", authorities).signWith(key);

		response.addHeader("Authentication", String.format("Bearer %s", tokenBuilder.compact()));

	}

}