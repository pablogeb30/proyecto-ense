package usc.etse.grei.ense.p3.project.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración de OpenAPI
 */
@Configuration
public class OpenAPIConfiguration {

	/**
	 * Metodo que crea un OpenAPI personalizado
	 *
	 * @return OpenAPI personalizado
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Enxeneria de software project")
						.version("3.0.0")
						.description("API")
						.contact(new Contact()
								.name("Pablo Liste Cancela")
								.email("pablo.liste@rai.usc.es")))
				.schemaRequirement("JWT", new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"));
	}

}