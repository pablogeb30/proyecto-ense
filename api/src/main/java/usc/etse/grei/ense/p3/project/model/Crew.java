package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Crew extends Person {

	@NotBlank(message = "The job field can not be empty", groups = {OnCreate.class, OnRelation.class})
	private String job;

	public Crew() {
	}

	public Crew(String job) {
		this.job = job;
	}

	public String getJob() {
		return job;
	}

	public Crew setJob(String job) {
		this.job = job;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Crew crew = (Crew) o;
		return Objects.equals(job, crew.job);
	}

	@Override
	public int hashCode() {
		return Objects.hash(job);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Crew.class.getSimpleName() + "[", "]")
				.add("job='" + job + "'")
				.toString();
	}

}