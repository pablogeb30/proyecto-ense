package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
	private String id;

	@NotBlank(message = "The title field can not be empty", groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class, OnRelation.class})
	private String title;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private String overview;

	@Size(min = 2, max = 256, groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private String tagline;

	@Valid
	@Null(groups = OnRelation.class)
	private Collection collection;

	@Null(groups = OnRelation.class)
	private List<@NotBlank(message = "The genre can not be empyt", groups = {OnCreate.class, OnUpdate.class}) String> genres;

	@Valid
	@Null(groups = OnRelation.class)
	private Date releaseDate;

	@Null(groups = OnRelation.class)
	private List<@NotBlank(message = "The genre can not be empyt", groups = {OnCreate.class, OnUpdate.class}) String> keywords;

	@Null(groups = OnRelation.class)
	private List<@Valid Producer> producers;

	@Null(groups = {OnCreate.class, OnRelation.class})
	private List<@Valid Crew> crew;

	@Null(groups = {OnCreate.class, OnRelation.class})
	private List<@Valid Cast> cast;

	@Null(groups = OnRelation.class)
	private List<@Valid Resource> resources;

	@Positive(groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private Long budget;

	@Null(groups = OnRelation.class)
	private Status status;

	@Positive(groups = {OnCreate.class, OnUpdate.class})
	@Null(groups = OnRelation.class)
	private Integer runtime;

	@Null(groups = OnRelation.class)
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