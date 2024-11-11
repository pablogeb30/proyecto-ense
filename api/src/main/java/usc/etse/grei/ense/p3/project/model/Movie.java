package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "movies")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movie {

	@Id
	@NotBlank(message = "The title field can not be empty", groups = OnRelation.class)
	@Null(groups = OnCreate.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The id of the movie",
			format = "string",
			type = "string",
			example = "1"
	)
	private String id;

	@NotBlank(message = "The title field can not be empty", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The title of the movie",
			format = "string",
			type = "string",
			example = "Inception",
			minLength = 1,
			maxLength = 256
	)
	private String title;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "A brief overview of the movie",
			format = "string",
			type = "string",
			example = "A thief who steals corporate secrets...",
			minLength = 1,
			maxLength = 512
	)
	private String overview;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The tagline of the movie",
			format = "string",
			type = "string",
			example = "Your mind is the scene of the crime",
			minLength = 1,
			maxLength = 256
	)
	private String tagline;

	@Valid
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The collection the movie belongs to",
			type = "object",
			implementation = Collection.class
	)
	private Collection collection;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of genres the movie belongs to",
			format = "array",
			type = "array"
	)
	private List<@NotBlank(message = "The genre can not be empyt", groups = {OnCreate.class, OnUpdate.class}) String> genres;

	@Valid
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The release date of the movie",
			format = "date",
			type = "string",
			example = "2010-08-06",
			minimum = "1900-01-01",
			maximum = "2025-01-01"
	)
	private Date releaseDate;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of keywords related to the movie",
			format = "array",
			type = "array"
	)
	private List<@NotBlank(message = "The keyword can not be empyt", groups = {OnCreate.class, OnUpdate.class}) String> keywords;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of producers involved in the movie",
			format = "array",
			type = "array"
	)
	private List<@Valid Producer> producers;

	@Null(groups = {OnCreate.class, OnRelation.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of crew members involved in the movie",
			format = "array",
			type = "array"
	)
	private List<@Valid Crew> crew;

	@Null(groups = {OnCreate.class, OnRelation.class})
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of cast members in the movie",
			format = "array",
			type = "array"
	)
	private List<@Valid Cast> cast;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "List of resources related to the movie",
			format = "array",
			type = "array",
			allowableValues = {"POSTER", "BACKDROP", "TRAILER", "NETFLIX", "AMAZON_PRIME", "DISNEY_PLUS", "ITUNES", "HBO", "YOUTUBE", "GOOGLE_PLAY", "TORRENT"}
	)
	private List<@Valid Resource> resources;

	@Positive(groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The budget of the movie in USD",
			format = "int64",
			type = "number",
			example = "160000000"
	)
	private Long budget;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The current status of the movie",
			type = "string",
			enumAsRef = true,
			allowableValues = {"RUMORED", "PLANNED", "PRODUCTION", "POSTPRODUCTION", "RELEASED", "CANCELLED"}
	)
	private Status status;

	@Positive(groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The runtime of the movie in minutes",
			format = "int32",
			type = "integer",
			example = "148"
	)
	private Integer runtime;

	@Null(groups = OnRelation.class)
	@Schema(
			requiredMode = Schema.RequiredMode.AUTO,
			description = "The revenue generated by the movie in USD",
			format = "int64",
			type = "number",
			example = "851532764"
	)
	private Long revenue;

	public Movie() {
	}

	public Movie(String id, String title, String overview, String tagline, Collection collection, List<String> genres, Date releaseDate, List<String> keywords, List<Producer> producers, List<Crew> crew, List<Cast> cast, List<Resource> resources, Long budget, Status status, Integer runtime, Long revenue) {
		this.id = id;
		this.title = title;
		this.overview = overview;
		this.tagline = tagline;
		this.collection = collection;
		this.genres = genres;
		this.releaseDate = releaseDate;
		this.keywords = keywords;
		this.producers = producers;
		this.crew = crew;
		this.cast = cast;
		this.resources = resources;
		this.budget = budget;
		this.status = status;
		this.runtime = runtime;
		this.revenue = revenue;
	}

	public String getId() {
		return id;
	}

	public Movie setId(String id) {
		this.id = id;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Movie setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getOverview() {
		return overview;
	}

	public Movie setOverview(String overview) {
		this.overview = overview;
		return this;
	}

	public String getTagline() {
		return tagline;
	}

	public Movie setTagline(String tagline) {
		this.tagline = tagline;
		return this;
	}

	public Collection getCollection() {
		return collection;
	}

	public Movie setCollection(Collection collection) {
		this.collection = collection;
		return this;
	}

	public List<String> getGenres() {
		return genres;
	}

	public Movie setGenres(List<String> genres) {
		this.genres = genres;
		return this;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public Movie setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
		return this;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public Movie setKeywords(List<String> keywords) {
		this.keywords = keywords;
		return this;
	}

	public List<Producer> getProducers() {
		return producers;
	}

	public Movie setProducers(List<Producer> producers) {
		this.producers = producers;
		return this;
	}

	public List<Crew> getCrew() {
		return crew;
	}

	public Movie setCrew(List<Crew> crew) {
		this.crew = crew;
		return this;
	}

	public List<Cast> getCast() {
		return cast;
	}

	public Movie setCast(List<Cast> cast) {
		this.cast = cast;
		return this;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public Movie setResources(List<Resource> resources) {
		this.resources = resources;
		return this;
	}

	public Long getBudget() {
		return budget;
	}

	public Movie setBudget(Long budget) {
		this.budget = budget;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public Movie setStatus(Status status) {
		this.status = status;
		return this;
	}

	public Integer getRuntime() {
		return runtime;
	}

	public Movie setRuntime(Integer runtime) {
		this.runtime = runtime;
		return this;
	}

	public Long getRevenue() {
		return revenue;
	}

	public Movie setRevenue(Long revenue) {
		this.revenue = revenue;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Movie movie = (Movie) o;
		return Objects.equals(id, movie.id) && Objects.equals(title, movie.title) && Objects.equals(overview, movie.overview) && Objects.equals(tagline, movie.tagline) && Objects.equals(collection, movie.collection) && Objects.equals(genres, movie.genres) && Objects.equals(releaseDate, movie.releaseDate) && Objects.equals(keywords, movie.keywords) && Objects.equals(producers, movie.producers) && Objects.equals(crew, movie.crew) && Objects.equals(cast, movie.cast) && Objects.equals(resources, movie.resources) && Objects.equals(budget, movie.budget) && status == movie.status && Objects.equals(runtime, movie.runtime) && Objects.equals(revenue, movie.revenue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, overview, tagline, collection, genres, releaseDate, keywords, producers, crew, cast, resources, budget, status, runtime, revenue);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Movie.class.getSimpleName() + "[", "]")
				.add("id='" + id + "'")
				.add("title='" + title + "'")
				.add("overview='" + overview + "'")
				.add("tagline='" + tagline + "'")
				.add("collection=" + collection)
				.add("genres=" + genres)
				.add("releaseDate=" + releaseDate)
				.add("keywords=" + keywords)
				.add("producers=" + producers)
				.add("crew=" + crew)
				.add("cast=" + cast)
				.add("resources=" + resources)
				.add("budget=" + budget)
				.add("status=" + status)
				.add("runtime=" + runtime)
				.add("revenue=" + revenue)
				.toString();
	}

}