package org.hivedb.hibernate.simplified.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.shards.Shard;
import org.hibernate.shards.session.ShardedSessionFactory;
import org.hibernate.shards.session.ShardedSessionImpl;
import org.hivedb.Hive;
import org.hivedb.configuration.EntityHiveConfig;
import org.hivedb.hibernate.HiveInterceptorDecorator;
import org.hivedb.hibernate.RecordNodeOpenSessionEvent;

import java.util.Map;

public class HiveSessionFactoryImpl implements HiveSessionFactory{
  private final static Log log = LogFactory.getLog(HiveSessionFactoryImpl.class);
  private ShardedSessionFactory factory;
  private EntityHiveConfig config;
  private Hive hive;
  private Map<Integer, SessionFactory> factories;
  
  public HiveSessionFactoryImpl(ShardedSessionFactory shardedFactory, Hive hive, EntityHiveConfig hiveConfig) {
    this.factory = shardedFactory;
    this.hive = hive;
    this.config = hiveConfig;
  }

  public Session openSession() {
    return openShardedSession(getDefaultInterceptor());
  }

  public Session openSession(Interceptor interceptor) {
    return openShardedSession(getDefaultInterceptor(interceptor));
  }

  public Session openSession(Object primaryIndexKey) {
    return null;
  }

  public Session openSession(Object primaryIndexKey, Interceptor interceptor) {
    return null;
  }

  public Session openSession(String resource, Object resourceId) {
    return null;
  }

  public Session openSession(String resource, Object resourceId, Interceptor interceptor) {
    return null;
  }

  public Session openSession(String resource, String indexName, Object secondaryIndexKey) {
    return null;
  }

  public Session openSession(String resource, String indexName, Object secondaryIndexKey, Interceptor interceptor) {
    return null;
  }

  public Interceptor getDefaultInterceptor() {
		return new HiveInterceptorDecorator(config, hive);
	}

  public Interceptor getDefaultInterceptor(Interceptor interceptor) {
		return new HiveInterceptorDecorator(interceptor, config, hive);
	}

  private Session openShardedSession(Interceptor interceptor) {
    return addEventsToShardedSession((ShardedSessionImpl) factory.openSession(getDefaultInterceptor(interceptor)));
  }
  
  private Session addEventsToShardedSession(ShardedSessionImpl session) {
		for (Shard shard : session.getShards()) {
			shard.addOpenSessionEvent(new RecordNodeOpenSessionEvent());
		}
		return session;
	}

  private Session addEventsToSession(Session session) {
		RecordNodeOpenSessionEvent.setNode(session);
		return session;
	}
}

