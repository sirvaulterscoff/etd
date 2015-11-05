package etd.util;

/**
 * @author pobedenniy.alexey
 * @since 03.11.2015
 */
public class Preconditions {
	public static void checkNotNull(Object obj, String message) {
		if (obj == null) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkTrue(boolean arg, String message) {
		if (!arg) {
			throw new IllegalArgumentException(message);
		}
	}
}
