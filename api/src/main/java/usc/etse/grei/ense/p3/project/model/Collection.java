package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
		name = "Collection",
		description = "The collection of resources representation"
)
public class Collection {

	@NotBlank(message = "The name field can not be empty", groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The name of the collection",
			format = "string",
			type = "string",
			minLength = 2,
			maxLength = 256,
			example = "Collection"
	)
	private String name;

	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The resources of the collection",
			format = "array",
			type = "array",
			allowableValues = {"POSTER", "BACKDROP", "TRAILER", "NETFLIX", "AMAZON_PRIME", "DISNEY_PLUS", "ITUNES", "HBO", "YOUTUBE", "GOOGLE_PLAY", "TORRENT"}
	)
	private List<@Valid Resource> resources;

	public Collection() {
	}

	public Collection(String name, List<Resource> resources) {
		this.name = name;
		this.resources = resources;
	}

	public String getName() {
		return name;
	}

	public Collection setName(String name) {
		this.name = name;
		return this;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public Collection setResources(List<Resource> resources) {
		this.resources = resources;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Collection that = (Collection) o;
		return Objects.equals(name, that.name) && Objects.equals(resources, that.resources);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, resources);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Collection.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("resources=" + resources)
				.toString();
	}

}