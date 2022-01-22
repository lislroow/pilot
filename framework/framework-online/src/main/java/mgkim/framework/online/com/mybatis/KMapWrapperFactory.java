package mgkim.framework.online.com.mybatis;

import java.util.Map;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.jdbc.support.JdbcUtils;

public class KMapWrapperFactory implements ObjectWrapperFactory {
	
	@Override
	public boolean hasWrapperFor(Object object) {
		return object != null && object instanceof Map;
	}
	
	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		return new KMapWrapper(metaObject, (Map) object);
	}
	
	class KMapWrapper extends MapWrapper {
		public KMapWrapper(MetaObject metaObject, Map<String, Object> map) {
			super(metaObject, map);
		}
		@Override
		public String findProperty(String name, boolean useCamelCaseMapping) {
			if (useCamelCaseMapping && ((name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') || name.contains("_"))) {
				return JdbcUtils.convertUnderscoreNameToPropertyName(name);
			}
			return name;
		}
	}
}

