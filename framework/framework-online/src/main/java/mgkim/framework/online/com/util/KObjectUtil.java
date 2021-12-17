package mgkim.framework.online.com.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.annotation.KTaskSchedule;

public class KObjectUtil {

	@SuppressWarnings("rawtypes")
	public static String name(Class clazz) {
		String name = null;
		Annotation[] list = clazz.getAnnotations();
		String aname = null;
		for(Annotation a : list) {
			if(a instanceof KBean) {
				aname = ((KBean)a).name();
				break;
			} else if(a instanceof KModule) {
				aname = ((KModule)a).name();
				break;
			} else if(a instanceof KTaskSchedule) {
				aname = ((KTaskSchedule)a).name();
				break;
			}
		}
		name = String.format("%s(`%s`)", clazz.getSimpleName(), aname);
		return name;
	}

	@SuppressWarnings("rawtypes")
	public static boolean required(Class clazz) {
		Annotation[] list = clazz.getAnnotations();
		boolean required = false;
		for(Annotation a : list) {
			if(a instanceof KModule) {
				required = ((KModule)a).required();
				break;
			}
		}
		return required;
	}

	@SuppressWarnings("rawtypes")
	public static int interval(Class clazz) {
		Annotation[] list = clazz.getAnnotations();
		int manage = 1000;
		for(Annotation a : list) {
			if(a instanceof KTaskSchedule) {
				manage = ((KTaskSchedule)a).interval();
				break;
			}
		}
		return manage;
	}

	@SuppressWarnings("rawtypes")
	public static boolean manage(Class clazz) {
		Annotation[] list = clazz.getAnnotations();
		boolean manage = false;
		for(Annotation a : list) {
			if(a instanceof KTaskSchedule) {
				manage = ((KTaskSchedule)a).manage();
				break;
			}
		}
		return manage;
	}

	public static Field[] getFieldList(Object obj) {
		java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
		return fields;
	}

	public static Field getField(Object obj, String fieldName) {
		for(Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch(NoSuchFieldException e) {
			}
		}
		return null;
	}

	public static Object getValue(Object obj, String fieldName) {
		Object value = null;
		try {
			Field field = getField(obj, fieldName);
			if(field != null) {
				if(field.isAccessible()) {
					value = field.get(obj);
				} else {
					field.setAccessible(true);
					value = field.get(obj);
					field.setAccessible(false);
				}
			}
		} catch(Exception e) {
			// nothing to do
		}
		return value;
	}

	public static void setValue(Object obj, String fieldName, Object value) {
		Field field = null;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
		} catch(NoSuchFieldException e) {
			try {
				field = obj.getClass().getSuperclass().getDeclaredField(fieldName);
			} catch(NoSuchFieldException e1) {
				try {
					if(obj.getClass().getSuperclass().getSuperclass() == null) {
						return;
					}
					field = obj.getClass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
				} catch (NoSuchFieldException e2) {
					// nothing to do
				} catch (SecurityException e2) {
					// nothing to do
				}
			}
		}
		try {
			if(field.isAccessible()) {
				field.set(obj, value);
			} else {
				field.setAccessible(true);
				if("int".equals(field.getType().getSimpleName())) {
					field.set(obj, Integer.parseInt(KStringUtil.nvl(value, "0")));
				} else if("long".equals(field.getType().getSimpleName())) {
					field.set(obj, Long.parseLong(KStringUtil.nvl(value, "0")));
				} else {
					field.set(obj, value);
				}
				field.setAccessible(false);
			}
		} catch(Exception e) {
			// nothing to do
		}
	}

	public static String getSqlParamByFieldName(Object obj, String fieldName)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field field = getField(obj, fieldName);
		String value = null;
		if(field != null) {
			field.setAccessible(true);
			if("int".equals(field.getType().getSimpleName()) || "long".equals(field.getType().getSimpleName()) || "double".equals(field.getType().getSimpleName())) {
				value = KStringUtil.nvl(field.get(obj));
			} else {
				value = KStringUtil.nvl(field.get(obj));
				if(value != null && value.getBytes().length > 1000) {
					value = KStringUtil.cut(value, 1000, 3, true) + " (*** omitted ***)";
				}
				value = "'"+value+"'";
			}
			field.setAccessible(false);
		}
		return value;
	}


	public static <T> T clone(final T obj) throws CloneNotSupportedException {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Cloneable) {
			final Class<?> clazz = obj.getClass();
			final Method m;
			try {
				m = clazz.getMethod("clone", (Class[]) null);
			} catch(final NoSuchMethodException ex) {
				throw new NoSuchMethodError(ex.getMessage());
			}
			try {
				@SuppressWarnings("unchecked") // OK because clone() preserves the class
				final T result = (T) m.invoke(obj, (Object[]) null);
				return result;
			} catch(final InvocationTargetException ex) {
				final Throwable cause = ex.getCause();
				if(cause instanceof CloneNotSupportedException) {
					throw ((CloneNotSupportedException) cause);
				} else {
					throw new Error("Unexpected exception", cause);
				}
			} catch(final IllegalAccessException ex) {
				throw new IllegalAccessError(ex.getMessage());
			}
		} else {
			throw new CloneNotSupportedException();
		}
	}
}