package org.hivedb.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hivedb.Hive;
import org.hivedb.HiveException;
import org.hivedb.meta.ColumnInfo;
import org.hivedb.meta.NodeGroup;
import org.hivedb.meta.PartitionDimension;
import org.hivedb.meta.PrimaryIndexIdentifiable;
import org.hivedb.meta.Resource;
import org.hivedb.meta.ResourceIdentifiable;
import org.hivedb.meta.SecondaryIndex;
import org.hivedb.meta.SecondaryIndexIdentifiable;
import org.hivedb.util.functional.RingIteratorable;
import org.hivedb.util.functional.Transform;
import org.hivedb.util.functional.Unary;
import org.hivedb.util.scenarioBuilder.HiveScenarioConfig;

public class InstallHiveIndexSchema {
	public static Map<PrimaryIndexIdentifiable, PartitionDimension> install(
			final HiveScenarioConfig hiveScenarioConfig,
			final Hive hive) throws HiveException {
		
		final RingIteratorable<String> indexUriIterator = new RingIteratorable<String>(hiveScenarioConfig.getIndexUris(hive));
	
		// Create partition dimensions and its subordinate NodeGroup, primary Node, Resources, and SecondaryIndexes
		Map<PrimaryIndexIdentifiable, PartitionDimension> partitionDimensionMap = Transform.toMap(
			new Transform.IdentityFunction<PrimaryIndexIdentifiable>(),
			new Unary<PrimaryIndexIdentifiable, PartitionDimension>() {
				public PartitionDimension f(final PrimaryIndexIdentifiable primaryIndexIdentifiable) {
					try {
						
						final NodeGroup nodeGroup = new NodeGroup(hiveScenarioConfig.getDataNodes());
						
						final Collection<Resource> resources = 
							Transform.map(new Unary<ResourceIdentifiable, Resource>() {
								public Resource f(ResourceIdentifiable resourceIdentifiable) { 
									return new Resource(resourceIdentifiable.getResourceName(), constructSecondaryIndexesOfResource(resourceIdentifiable));
								}},
								primaryIndexIdentifiable.getResourceIdentifiables());
						PartitionDimension partitionDimension = new PartitionDimension(
							primaryIndexIdentifiable.getPartitionDimensionName(),
							JdbcTypeMapper.primitiveTypeToJdbcType(primaryIndexIdentifiable.getPrimaryIndexKey().getClass()),
							nodeGroup,
							indexUriIterator.next(),
							resources
						);
						hive.addPartitionDimension(partitionDimension);
						return partitionDimension;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}},
				Collections.singleton(hiveScenarioConfig.getPrimaryIndexIdentifiable()));
		
		try {
			hive.create();
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
		return partitionDimensionMap;
	}
	
	public static Collection<SecondaryIndex> constructSecondaryIndexesOfResource(final ResourceIdentifiable resourceIdentifiable) {	
		try {
			return 
				Transform.map(
					new Unary<SecondaryIndexIdentifiable, SecondaryIndex>() {
						public SecondaryIndex f(SecondaryIndexIdentifiable secondaryIndexIdentifiable) {
							try {
								return new SecondaryIndex(
										new ColumnInfo(
											secondaryIndexIdentifiable.getSecondaryIndexColumnName(),											
											JdbcTypeMapper.primitiveTypeToJdbcType(secondaryIndexIdentifiable.getSecondaryIndexKey().getClass())));
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
					}}, 
					resourceIdentifiable.getSecondaryIndexIdentifiables());
					
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
}
