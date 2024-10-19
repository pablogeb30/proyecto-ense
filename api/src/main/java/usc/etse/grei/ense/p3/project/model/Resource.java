package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resource {

	@URL(message = "The url field must be and URL", groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	@NotBlank(message = "The url field is required", groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	private String url;

	@NotNull(message = "The type can not b null", groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	private ResourceType type;

	public Resource(ResourceType type, String url) {
		this.type = type;
		this.url = url;
	}

	public Resource() {
	}

	public ResourceType getType() {
		return type;
	}

	public Resource setType(ResourceType type) {
		this.type = type;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Resource setUrl(String url) {
		this.url = url;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Resource resource = (Resource) o;
		return Objects.equals(url, resource.url) && type == resource.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, type);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Resource.class.getSimpleName() + "[", "]")
				.add("url='" + url + "'")
				.add("type=" + type)
				.toString();
	}

}