package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
		name = "Date",
		description = "Date representation"
)
public class Date {

	@NotNull(message = "The day field can not be empty", groups = {OnCreate.class, OnUpdate.class})
	@Range(min = 1, max = 31, message = "The day field must be between 1 and 31", groups = {OnCreate.class, OnUpdate.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The day of the date",
			format = "int32",
			type = "integer",
			minimum = "1",
			maximum = "31",
			example = "1"
	)
	private Integer day;

	@NotNull(message = "The month field can not be empty", groups = {OnCreate.class, OnUpdate.class})
	@Range(min = 1, max = 12, message = "The month field must be between 1 and 12", groups = {OnCreate.class, OnUpdate.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The month of the date",
			format = "int32",
			type = "integer",
			minimum = "1",
			maximum = "12",
			example = "1"
	)
	private Integer month;

	@NotNull(message = "The year field can not be empty", groups = {OnCreate.class, OnUpdate.class})
	@Range(min = 1900, max = 2024, message = "The year field must be between 1900 and 2100", groups = {OnCreate.class, OnUpdate.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The year of the date",
			format = "int32",
			type = "integer",
			minimum = "1900",
			maximum = "2024",
			example = "2024"
	)
	private Integer year;

	public Date() {

	}

	public Date(Integer day, Integer month, Integer year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public Integer getDay() {
		return day;
	}

	public Date setDay(Integer day) {
		this.day = day;
		return this;
	}

	public Integer getMonth() {
		return month;
	}

	public Date setMonth(Integer month) {
		this.month = month;
		return this;
	}

	public Integer getYear() {
		return year;
	}

	public Date setYear(Integer year) {
		this.year = year;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Date date = (Date) o;
		return Objects.equals(day, date.day) && Objects.equals(month, date.month) && Objects.equals(year, date.year);
	}

	@Override
	public int hashCode() {
		return Objects.hash(day, month, year);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Date.class.getSimpleName() + "[", "]")
				.add("day=" + day)
				.add("month=" + month)
				.add("year=" + year)
				.toString();
	}

}