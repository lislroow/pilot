package mgkim.online.com.util;

import java.util.EnumSet;

public class KEnumUtil {

	public static <E extends Enum<E>> E get(Class<E> e, String val) {
		EnumSet<E> values = EnumSet.allOf(e);
		for(E item : values) {
			if(item.name().toLowerCase().equals(val)) {
				return item;
			}
		};
		return null;
	}
}
