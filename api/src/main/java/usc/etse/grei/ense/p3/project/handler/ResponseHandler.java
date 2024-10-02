package usc.etse.grei.ense.p3.project.handler;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {

	public static ResponseEntity<Object> generateResponse(boolean error, String message, int code, Object responseObj, EntityModel entityModel, HttpStatus status) {

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("error", error);
		map.put("code", code);
		map.put("message", message);
		map.put("data", responseObj);
		map.put("_links", entityModel.getLinks());

		return new ResponseEntity<Object>(map, status);

	}

}