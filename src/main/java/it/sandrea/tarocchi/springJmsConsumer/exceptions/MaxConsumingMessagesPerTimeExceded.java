package it.sandrea.tarocchi.springJmsConsumer.exceptions;

public class MaxConsumingMessagesPerTimeExceded extends Exception {
	private static final long serialVersionUID = 1L;

	public String message;
	
	public MaxConsumingMessagesPerTimeExceded(String message){
		this.message=message;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage()+this.message;
	}
}
