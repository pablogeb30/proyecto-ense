package usc.etse.grei.ense.p3.project.util;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio que implementa los criterios de ordenación
 */
public class SortUtil {

	/**
	 * Metodo que convierte una lista de cadenas en criterios de ordenación
	 *
	 * @param sort cadenas con los criterios para cada campo
	 * @return criterios de ordenación
	 */
	public static List<Sort.Order> getCriteria(List<String> sort) {

		List<Sort.Order> criteria = sort.stream().map(string -> {
			if (string.startsWith("+")) {
				return Sort.Order.asc(string.substring(1));
			} else if (string.startsWith("-")) {
				return Sort.Order.desc(string.substring(1));
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());

		return criteria;

	}

}