package usc.etse.grei.ense.p3.project.model;

import org.springframework.http.HttpStatus;

public class Result<T> {

	private T result;
	private boolean error;
	private String messaje;
	private Integer internalCode;
	private Code externalCode;
	public Result() {
	}

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

	public HttpStatus getStatus() {

		switch (externalCode) {
			case OK:
				return HttpStatus.OK;
			case CREATED:
				return HttpStatus.CREATED;
			case ACCEPTED:
				return HttpStatus.ACCEPTED;
			case NO_CONTENT:
				return HttpStatus.NO_CONTENT;
			case BAD_REQUEST:
				return HttpStatus.BAD_REQUEST;
			case UNAUTHORIZED:
				return HttpStatus.UNAUTHORIZED;
			case FORBIDDEN:
				return HttpStatus.FORBIDDEN;
			case NOT_FOUND:
				return HttpStatus.NOT_FOUND;
			case CONFLICT:
				return HttpStatus.CONFLICT;
			default:
				return HttpStatus.BAD_REQUEST;
		}

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

		public int getCode() {
			return code;
		}

	}

}