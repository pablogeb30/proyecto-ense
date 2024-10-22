package usc.etse.grei.ense.p3.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la API que inicia su ejecución
 */
@SpringBootApplication
public class ProjectApplication {

    /**
     * Metodo principal que inicia la API
     *
     * @param args argumentos de ejecución
     */
    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }

}
