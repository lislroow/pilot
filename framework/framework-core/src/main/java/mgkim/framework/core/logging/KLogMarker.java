package mgkim.framework.core.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class KLogMarker {
	
	public static final Marker REQUEST = MarkerFactory.getMarker("[ +++ REQUEST +++ ]");
	public static final Marker RESPONSE = MarkerFactory.getMarker("[ --- RESPONSE --- ]");
	public static final Marker SQL = MarkerFactory.getMarker("[ *** sql *** ]");
	public static final Marker request = MarkerFactory.getMarker("[ *** request *** ]");
	public static final Marker response = MarkerFactory.getMarker("[ *** response *** ]");
	public static final Marker security = MarkerFactory.getMarker("[ *** security *** ]");
	
}
