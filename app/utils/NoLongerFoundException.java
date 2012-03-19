package utils;

/**
 * Exception thrown when track is no longer in the tracklist. This will occur
 * when in the meantime a filesystem refresh has been done and the track was
 * removed.
 */
public class NoLongerFoundException extends Exception {

	public NoLongerFoundException(String message) {
		super(message);
	}

}
