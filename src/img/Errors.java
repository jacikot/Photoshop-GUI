package img;

public class Errors extends Exception {
	public Errors() {
		super("Error");
	}
	public Errors(String message) {
		super(message);
	}
	@Override
	public String toString() {
		return getMessage();
	}
}
