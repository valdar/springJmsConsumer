package it.sandrea.tarocchi.springJmsConsumer.exceptions;

public class UnsupportedMessageTypeException extends Exception {
	private static final long serialVersionUID = 1L;

	public String message;
	
	public UnsupportedMessageTypeException(String message){
		this.message=message;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage()+this.message;
	}
}
