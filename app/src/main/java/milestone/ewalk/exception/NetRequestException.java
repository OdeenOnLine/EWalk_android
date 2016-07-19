package milestone.ewalk.exception;

public class NetRequestException extends Exception {

	private Error error;

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public NetRequestException(Error error) {
		super();
		// TODO Auto-generated constructor stub
		this.error = error;
	}

	public NetRequestException(String detailMessage, Throwable throwable,
                               Error error) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
		this.error = error;
	}

	public NetRequestException(String detailMessage, Error error) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
		this.error = error;
	}

	public NetRequestException(Throwable throwable, Error error) {
		super(throwable);
		// TODO Auto-generated constructor stub
		this.error = error;
	}
	
	
}
