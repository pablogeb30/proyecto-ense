package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "assessments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Assessment {

	@Id
	@NotNull(message = "The id field can not be empty", groups = OnUpdate.class)
	@Null(message = "The rating field need to be null", groups = {OnUserCreate.class, OnMovieCreate.class})
	private String id;

	@NotNull(message = "The rating field can not be empty", groups = {OnUserCreate.class, OnMovieCreate.class, OnUpdate.class})
	@Range(min = 1, max = 10, groups = {OnUserCreate.class, OnMovieCreate.class, OnUpdate.class})
	private Integer rating;

	@Valid
	@NotNull(message = "The user field can not be empty", groups = {OnMovieCreate.class, OnUpdate.class})
	private User user;

	@Valid
	@NotNull(message = "The movie field can not be empty", groups = {OnUserCreate.class, OnUpdate.class})
	private Movie movie;

	@Size(min = 1, max = 500, message = "The comment field must be between 1 and 500 characters", groups = {OnUserCreate.class, OnMovieCreate.class, OnUpdate.class})
	private String comment;

	public Assessment() {
	}

	public Assessment(String id, Integer rating, User user, Movie movie, String comment) {
		this.id = id;
		this.rating = rating;
		this.user = user;
		this.movie = movie;
		this.comment = comment;
	}

	public String getId() {
		return id;
	}

	public Assessment setId(String id) {
		this.id = id;
		return this;
	}

	public Integer getRating() {
		return rating;
	}

	public Assessment setRating(Integer rating) {
		this.rating = rating;
		return this;
	}

	public User getUser() {
		return user;
	}

	public Assessment setUser(User user) {
		this.user = user;
		return this;
	}

	public Movie getMovie() {
		return movie;
	}

	public Assessment setMovie(Movie movie) {
		this.movie = movie;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public Assessment setComment(String comment) {
		this.comment = comment;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Assessment that = (Assessment) o;
		return Objects.equals(id, that.id) && Objects.equals(rating, that.rating) && Objects.equals(user, that.user) && Objects.equals(movie, that.movie) && Objects.equals(comment, that.comment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rating, user, movie, comment);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Assessment.class.getSimpleName() + "[", "]")
				.add("id='" + id + "'")
				.add("rating=" + rating)
				.add("user=" + user)
				.add("movie=" + movie)
				.add("comment='" + comment + "'")
				.toString();
	}

}