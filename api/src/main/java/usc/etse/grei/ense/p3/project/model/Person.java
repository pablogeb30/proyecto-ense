package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "persons")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
		name = "Person",
		description = "A person representation"
)
public class Person {

	@Id
	@NotBlank(message = "The id field can not be empty", groups = {OnUpdate.class, OnRelation.class})
	@Null(groups = OnCreate.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The id of the person",
			format = "string",
			type = "string",
			example = "1"
	)
	private String id;

	@NotBlank(message = "The name field can not be empty", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "Nombre",
			description = "The name of the person",
			format = "string",
			type = "string",
			minLength = 2,
			maxLength = 256
	)
	private String name;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "Spain",
			description = "The country of the person",
			format = "string",
			type = "string",
			minLength = 2,
			maxLength = 256
	)
	private String country;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "https://www.google.com",
			description = "The picture of the person",
			format = "url",
			type = "string",
			pattern = "^(http|https)://.*$"
	)
	private String picture;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "Biography",
			description = "The biography of the person",
			format = "string",
			type = "string",
			minLength = 2,
			maxLength = 256
	)
	private String biography;

	@Valid
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "2021-10-10",
			description = "The birthday of the person",
			format = "date",
			type = "string",
			minimum = "1900",
			maximum = "2024"
	)
	private Date birthday;

	@Valid
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			example = "2021-10-10",
			description = "The deathday of the person",
			format = "date",
			type = "string",
			minimum = "1900",
			maximum = "2024"
	)
	private Date deathday;

	public Person() {
	}

	public Person(String id, String name, String country, String picture, String biography, Date birthday, Date deathday) {
		this.id = id;
		this.name = name;
		this.country = country;
		this.picture = picture;
		this.biography = biography;
		this.birthday = birthday;
		this.deathday = deathday;
	}

	public String getId() {
		return id;
	}

	public Person setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Person setName(String name) {
		this.name = name;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public Person setCountry(String country) {
		this.country = country;
		return this;
	}

	public String getPicture() {
		return picture;
	}

	public Person setPicture(String picture) {
		this.picture = picture;
		return this;
	}

	public String getBiography() {
		return biography;
	}

	public Person setBiography(String biography) {
		this.biography = biography;
		return this;
	}

	public Date getBirthday() {
		return birthday;
	}

	public Person setBirthday(Date birthday) {
		this.birthday = birthday;
		return this;
	}

	public Date getDeathday() {
		return deathday;
	}

	public Person setDeathday(Date deathday) {
		this.deathday = deathday;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Person person = (Person) o;
		return Objects.equals(id, person.id) && Objects.equals(name, person.name) && Objects.equals(country, person.country) && Objects.equals(picture, person.picture) && Objects.equals(biography, person.biography) && Objects.equals(birthday, person.birthday) && Objects.equals(deathday, person.deathday);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, country, picture, biography, birthday, deathday);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Person.class.getSimpleName() + "[", "]")
				.add("id='" + id + "'")
				.add("name='" + name + "'")
				.add("country='" + country + "'")
				.add("picture='" + picture + "'")
				.add("biography='" + biography + "'")
				.add("birthday=" + birthday)
				.add("deathday=" + deathday)
				.toString();
	}

}