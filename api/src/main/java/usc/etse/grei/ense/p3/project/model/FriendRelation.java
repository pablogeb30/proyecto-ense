package usc.etse.grei.ense.p3.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendRelation {

	@Email
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String friendEmail;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String friendName;

	@NotNull(groups = OnUpdate.class)
	private FriendStatus status;

	@NotNull(groups = OnUpdate.class)
	private Date requested;

	private Date accepted;

	public FriendRelation() {

	}

	public FriendRelation(String friendEmail, String friendName, FriendStatus status, Date requested, Date accepted) {
		this.friendEmail = friendEmail;
		this.friendName = friendName;
		this.status = status;
		this.requested = requested;
		this.accepted = accepted;
	}

	public String getFriendEmail() {
		return friendEmail;
	}

	public FriendRelation setFriend(String friend) {
		this.friendEmail = friend;
		return this;
	}

	public String getFriendName() {
		return friendName;
	}

	public FriendRelation setFriendName(String friendName) {
		this.friendName = friendName;
		return this;
	}

	public FriendStatus getStatus() {
		return status;
	}

	public FriendRelation setStatus(FriendStatus status) {
		this.status = status;
		return this;
	}

	public Date getRequested() {
		return requested;
	}

	public FriendRelation setRequested(Date requested) {
		this.requested = requested;
		return this;
	}

	public Date getAccepted() {
		return accepted;
	}

	public FriendRelation setAccepted(Date accepted) {
		this.accepted = accepted;
		return this;
	}

}