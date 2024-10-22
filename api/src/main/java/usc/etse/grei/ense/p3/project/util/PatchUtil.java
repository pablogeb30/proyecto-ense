package usc.etse.grei.ense.p3.project.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio que implementa las actualizaciones parciales de objetos
 */
@Service
public class PatchUtil {

	private final ObjectMapper mapper;

	@Autowired
	public PatchUtil(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Metodo que aplica una lista de modificaciones a un objeto utilizando JSONPatch
	 *
	 * @param data    objeto al que se le aplican las modificaciones
	 * @param updates lista de operaciones
	 * @param <T>     metodo genérico
	 * @return objeto modificado
	 * @throws JsonPatchException excepcion
	 */
	@SuppressWarnings("unchecked")
	public <T> T patch(T data, List<Map<String, Object>> updates) throws JsonPatchException {

		JsonPatch operations = mapper.convertValue(updates, JsonPatch.class);
		JsonNode json = mapper.convertValue(data, JsonNode.class);
		JsonNode updatedJson = operations.apply(json);
		return (T) mapper.convertValue(updatedJson, data.getClass());

	}

}