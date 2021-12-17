package mgkim.core.com.scheduler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;

import mgkim.core.com.annotation.KTaskSchedule;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.stereo.KScheduler;
import mgkim.core.com.stereo.KTask;
import mgkim.core.com.util.KSqlUtil;

@KTaskSchedule(name = "sqlmap-file-reload 스케줄러", interval = 500, manage = false)
public class ComSqlmapReloadScheduler extends KScheduler {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	@Autowired
	SqlSessionFactoryBean sqlSessionFactoryBean;

	@Autowired
	@Qualifier("mapperLocations")
	Resource[] mapperLocations;

	private static Set<MappedStatement> MAPPED_STATEMENT_LIST = null;

	public static Set<MappedStatement> getMappedStatements() {
		return MAPPED_STATEMENT_LIST;
	}

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
		}

		// mapperLocations 확인
		//try {
		//	mapperLocations = KMybatisUtil.getMapperLocations();
		//} catch (Exception e) {
		//	e.printStackTrace();
		//}

		// mappedStatement 목록 불러오기
		{
			reloadStatement();
		}
	}

	@Override
	protected KTask task() throws Exception {
		KTask task = new KTask() {
			Map<Resource, Long> map = new HashMap<Resource, Long>();
			@Override
			protected void execute(String execId) throws Exception {
				if(isModified()) {
					w.lock();
					try {
						try {
							sqlSessionFactoryBean.afterPropertiesSet();
							reloadStatement();
						} catch(org.apache.ibatis.type.TypeException e) {
							throw new KSysException(KMessage.E5102);
						}
					} catch(Exception e) {
						throw e;
					} finally {
						w.unlock();
					}
				} else {
					// isn't changed ...
				}
			}

			private boolean isModified() {
				boolean retVal = false;
				if(mapperLocations != null) {
					for (int i = 0; i < mapperLocations.length; i++) {
						Resource mappingLocation = mapperLocations[i];
						retVal |= findModifiedResource(mappingLocation);
					}
				}
				return retVal;
			}

			private boolean findModifiedResource(Resource resource) {
				boolean retVal = false;
				List<String> modifiedResources = new ArrayList<String>();
				if(!resource.exists()) {
					return false;
				}
				try {
					long modified = resource.lastModified();

					if(map.containsKey(resource)) {
						long lastModified = ((Long) map.get(resource)).longValue();

						if(lastModified != modified) {
							map.put(resource, new Long(modified));
							modifiedResources.add(KSqlUtil.getRelativePath(resource.getDescription()));
							retVal = true;
						}
					} else {
						map.put(resource, new Long(modified));
					}
				} catch (IOException e) {
					KLogSys.error("caught exception", e);
				}
				if(retVal) {
					KLogSys.warn("modified files " + modifiedResources);
				}
				return retVal;
			}
		};
		return task;
	}

	private void reloadStatement() {
		SqlSessionFactory factory = (SqlSessionFactory) Proxy.newProxyInstance(
				SqlSessionFactory.class.getClassLoader(),
				new Class[]{SqlSessionFactory.class},
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						r.lock();
						SqlSessionFactory sqlSessionFactory = null;
						try {
							sqlSessionFactory = sqlSessionFactoryBean.getObject();
						} finally {
							r.unlock();
						}
						return method.invoke(sqlSessionFactory, args);
					}
			});
		MAPPED_STATEMENT_LIST = factory.getConfiguration().getMappedStatements().stream().collect(Collectors.toSet());
	}

	public static String getSqlFile(String sqlId) {
		MappedStatement mappedStatement = null;
		Iterator<MappedStatement> iter = MAPPED_STATEMENT_LIST.iterator();
		while(iter.hasNext()) {
			Object obj = iter.next();
			if(obj instanceof MappedStatement) {
				MappedStatement ms = (MappedStatement)obj;
				if(ms.getId().equals(sqlId)) {
					mappedStatement = ms;
					break;
				}
			}
		}

		if(mappedStatement == null) {
			return null;
		}

		return new java.io.File(mappedStatement.getResource().replaceAll("file \\[(.*)\\]", "$1")).getAbsolutePath()
			.replace(new java.io.File(KConstant.PATH_WEBINF_CLASSES).getAbsolutePath(), "")
			.replaceAll("\\\\", "/").substring(1);
	}

}