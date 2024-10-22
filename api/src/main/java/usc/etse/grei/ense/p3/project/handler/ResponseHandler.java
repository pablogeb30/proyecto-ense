package usc.etse.grei.ense.p3.project.handler;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
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
	 * @param entityModel enlaces a la entidad
	 * @param status      código de estado
	 * @return respuesta HTTP
	 */
	public static ResponseEntity<Object> generateResponse(boolean error, String message, int code, Object responseObj, EntityModel entityModel, HttpStatus status) {

		Map<String, Object> map = new HashMap<>();

		map.put("error", error);
		map.put("code", code);
		map.put("message", message);
		map.put("data", responseObj);
		map.put("_links", entityModel.getLinks());

		return new ResponseEntity<>(map, status);

	}

}