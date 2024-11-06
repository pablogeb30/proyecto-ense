package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.User;
import usc.etse.grei.ense.p3.project.repository.UserRepository;

/**
 * Servicio que implementa la lógica de negocio para la autenticación
 */
@Service
public class AuthenticationService implements UserDetailsService {

	private final UserRepository users;

	@Autowired
	public AuthenticationService(UserRepository users) {
		this.users = users;
	}

	/**
	 * Metodo que obtiene un usuario con los detalles de seguridad para autenticación y autorización
	 *
	 * @param username nombre del usuario
	 * @return detalles de seguridad del usuario
	 * @throws UsernameNotFoundException excepcion
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = users.findById(username).orElseThrow(() -> new UsernameNotFoundException(username));

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(
						String.join(",", user.getRoles())
				))
				.build();
	}

}