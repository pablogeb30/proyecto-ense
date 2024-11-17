package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
		name = "Cast",
		description = "A cast member of a movie"
)
public class Cast extends Person {

	@NotBlank(message = "The character field can not be empty", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The character of the cast",
			format = "string",
			type = "string",
			example = "Character",
			minLength = 2,
			maxLength = 256
	)
	private String character;

	@NotNull(message = "The relationId field can not be empty", groups = {OnCreate.class, OnUpdate.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The relation id of the cast",
			format = "int32",
			type = "integer",
			example = "1"
	)
	private Integer relationId;

	public Cast() {
	}

	public Cast(String id, String name, String country, String picture, String biography, Date birthday, Date deathday, String character) {
		super(id, name, country, picture, biography, birthday, deathday);
		this.character = character;
	}

	public String getCharacter() {
		return character;
	}

	public Cast setCharacter(String character) {
		this.character = character;
		return this;
	}

	public Integer getRelationId() {
		return relationId;
	}

	public Cast setRelationId(Integer relationId) {
		this.relationId = relationId;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Cast cast = (Cast) o;
		return Objects.equals(character, cast.character);
	}

	@Override
	public int hashCode() {
		return Objects.hash(character, super.hashCode());
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Cast.class.getSimpleName() + "[", "]")
				.add("character='" + character + "'")
				.toString();
	}

}