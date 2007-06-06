package org.hivedb.util;

import java.util.Collection;

import org.hivedb.Hive;
import org.hivedb.HiveException;
import org.hivedb.meta.Finder;
import org.hivedb.meta.Nameable;

public class HiveFinder implements Finder {
	private Hive hive;
	
	public HiveFinder(Hive hive) {
		this.hive = hive;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Nameable> T findByName(Class<T> forClass, String name)
			throws HiveException {
			return (T) hive.getPartitionDimension(name);
	}

	@SuppressWarnings("unchecked")
	public <T extends Nameable> Collection<T> findCollection(Class<T> forClass) {
		 return (Collection<T>) hive.getPartitionDimensions();
	}

}