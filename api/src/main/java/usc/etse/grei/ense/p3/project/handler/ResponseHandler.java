package usc.etse.grei.ense.p3.project.handler;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que funciona como manejador de respuestas HTTP
 */
public class ResponseHandler {

	/**
	 * Metodo que centraliza la creación de respuestas HTTP
	 *
	 * @param error       presencia o ausencia de error
	 * @param message     mensaje descriptivo
	 * @param code        código numérico
	 * @param responseObj datos que se devuelven en la respuesta
	 * @param links       enlaces HATEOAS
	 * @param status      código de estado
	 * @return respuesta HTTP
	 */
	public static ResponseEntity<Object> generateResponse(boolean error, String message, int code, Object responseObj, List<Link> links, HttpStatus status) {

		Map<String, Object> map = new HashMap<>();

		map.put("error", error);
		map.put("code", code);
		map.put("message", message);
		map.put("data", responseObj);

		HttpHeaders headers = new HttpHeaders();

		for (Link link : links) {
			headers.add(HttpHeaders.LINK, link.toString());
		}

		return new ResponseEntity<>(map, headers, status);

	}

}