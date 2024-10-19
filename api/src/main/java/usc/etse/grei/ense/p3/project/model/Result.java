package usc.etse.grei.ense.p3.project.model;

import org.springframework.http.HttpStatus;

public class Result<T> {

	private final T result;
	private final boolean error;
	private final String messaje;
	private final Integer internalCode;
	private final Code externalCode;

	public Result(T result, boolean error, String messaje, Integer internalCode, Code externalCode) {
		this.result = result;
		this.error = error;
		this.messaje = messaje;
		this.internalCode = internalCode;
		this.externalCode = externalCode;
	}

	public T getResult() {
		return result;
	}

	public boolean isError() {
		return error;
	}

	public String getMessaje() {
		return messaje;
	}

	public Integer getInternalCode() {
		return internalCode;
	}

	public Code getExternalCode() {
		return externalCode;
	}

	public HttpStatus getStatus() {

		return switch (externalCode) {
			case OK -> HttpStatus.OK;
			case CREATED -> HttpStatus.CREATED;
			case ACCEPTED -> HttpStatus.ACCEPTED;
			case NO_CONTENT -> HttpStatus.NO_CONTENT;
			case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
			case FORBIDDEN -> HttpStatus.FORBIDDEN;
			case NOT_FOUND -> HttpStatus.NOT_FOUND;
			case CONFLICT -> HttpStatus.CONFLICT;
			default -> HttpStatus.BAD_REQUEST;
		};

	}

	public enum Code {

		OK(200),
		CREATED(201),
		ACCEPTED(202),
		NO_CONTENT(204),
		BAD_REQUEST(400),
		UNAUTHORIZED(401),
		FORBIDDEN(403),
		NOT_FOUND(404),
		CONFLICT(409);

		private final int code;

		Code(int code) {
			this.code = code;
		}

	}

}