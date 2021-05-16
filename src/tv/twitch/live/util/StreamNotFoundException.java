package tv.twitch.live.util;

import java.io.IOException;

public class StreamNotFoundException extends IOException {
	private static final long serialVersionUID = 7191279019643645392L;
	
	public StreamNotFoundException() {
		super("Stream not found (404)");
	}
	
	public StreamNotFoundException(String message) {
		super(message);
	}
	
	public StreamNotFoundException(Throwable cause) {
		super(cause);
	}
	
	public StreamNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
