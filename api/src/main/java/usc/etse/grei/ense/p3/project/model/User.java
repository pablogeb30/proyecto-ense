package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

	@Id
	@NotBlank(message = "The email field can not be empty", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Email(message = "The email field need to be a valid email", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	private String email;

	@NotBlank(message = "The name field can not be empty", groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnRelation.class, OnUpdate.class})
	private String name;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private String country;

	@URL(message = "The picture field need to be a valid URL", groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private String picture;

	@Valid
	@NotNull(message = "The birthday field is required", groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private Date birthday;

	@Null(message = "The friends field is not allowed", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	private List<@Valid User> friends;

	@NotBlank(message = "The password field can not be empty", groups = {OnCreate.class, OnUpdate.class})
	@Null(message = "The password field is not allowed", groups = OnRelation.class)
	private String password;

	@Null(message = "The friends field is not allowed", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	private List<String> roles;

	public User() {
	}

	public User(String email, String name, String country, String picture, Date birthday, List<User> friends) {
		this.email = email;
		this.name = name;
		this.country = country;
		this.picture = picture;
		this.birthday = birthday;
		this.friends = friends;
	}

	public String getEmail() {
		return email;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getName() {
		return name;
	}

	public User setName(String name) {
		this.name = name;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public User setCountry(String country) {
		this.country = country;
		return this;
	}

	public String getPicture() {
		return picture;
	}

	public User setPicture(String picture) {
		this.picture = picture;
		return this;
	}

	public Date getBirthday() {
		return birthday;
	}

	public User setBirthday(Date birthday) {
		this.birthday = birthday;
		return this;
	}

	public List<User> getFriends() {
		return friends;
	}

	public User setFriends(List<User> friends) {
		this.friends = friends;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public User setPassword(String password) {
		this.password = password;
		return this;
	}

	public List<String> getRoles() {
		return roles;
	}

	public User setRoles(List<String> roles) {
		this.roles = roles;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(email, user.email) && Objects.equals(name, user.name) && Objects.equals(country, user.country) && Objects.equals(picture, user.picture) && Objects.equals(birthday, user.birthday) && Objects.equals(friends, user.friends);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, name, country, picture, birthday, friends);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
				.add("email='" + email + "'")
				.add("name='" + name + "'")
				.add("country='" + country + "'")
				.add("picture='" + picture + "'")
				.add("birthday=" + birthday)
				.add("friends=" + friends)
				.toString();
	}

}