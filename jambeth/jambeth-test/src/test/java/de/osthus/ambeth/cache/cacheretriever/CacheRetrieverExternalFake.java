package de.osthus.ambeth.cache.cacheretriever;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.cache.transfer.ObjRelationResult;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.util.IPrintable;

public class CacheRetrieverExternalFake implements ICacheService
{
	@LogInstance
	private ILogger log;

	private static final List<String> toString = Arrays.<String> asList("oh", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine");

	protected final Set<Class<?>> entityTypes = new HashSet<Class<?>>(Arrays.<Class<?>> asList(ExternalEntity.class, ExternalEntity2.class));

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		if (log.isDebugEnabled())
		{
			debugToLoad(orisToLoad);
		}
		List<ILoadContainer> targetEntities = new ArrayList<ILoadContainer>(orisToLoad.size());

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRef ori = orisToLoad.get(i);
			Class<?> entityType = ori.getRealType();
			if (!entityTypes.contains(entityType))
			{
				throw new IllegalArgumentException("This cache service does not handle entities of type '" + entityType.getName() + "'");
			}

			IObjRef primaryOri = getPrimaryOri(ori);
			String name = getName(ori);
			Integer checksum = Integer.valueOf(checksum((Integer) primaryOri.getId()));

			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			IObjRef[] parentRelation = getParentRelation(primaryOri, checksum);
			IObjRef[][] relations = getRelations(metaData, parentRelation);

			LoadContainer loadContainer = new LoadContainer();
			loadContainer.setReference(primaryOri);
			loadContainer.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
			loadContainer.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = name;
			loadContainer.getPrimitives()[metaData.getIndexByPrimitiveName("Value")] = name.length();
			loadContainer.setRelations(relations);

			targetEntities.add(loadContainer);
		}

		return targetEntities;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		List<IObjRelationResult> results = new ArrayList<IObjRelationResult>(objRelations.size());
		for (IObjRelation objRelation : objRelations)
		{
			Class<?> entityType = objRelation.getRealType();
			if (!entityTypes.contains(entityType))
			{
				throw new IllegalArgumentException("This cache service does not handle entities of type '" + entityType.getName() + "'");
			}

			IObjRef ori = objRelation.getObjRefs()[0];

			IObjRef primaryOri = getPrimaryOri(ori);
			Integer checksum = Integer.valueOf(checksum((Integer) primaryOri.getId()));

			IObjRef[] parentRelation = getParentRelation(primaryOri, checksum);

			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

			IObjRef[] relations;
			String memberName = objRelation.getMemberName();
			if ("Parent".equals(memberName))
			{
				relations = parentRelation;
			}
			else if ("Local".equals(memberName))
			{
				IObjRef[][] allRelations = getRelations(metaData, parentRelation);
				relations = allRelations[1];
			}
			else
			{
				throw new IllegalArgumentException("Unknown member '" + memberName + "'");
			}

			ObjRelationResult orr = new ObjRelationResult();
			orr.setReference(objRelation);
			orr.setRelations(relations);
		}
		return results;
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription)
	{
		throw new UnsupportedOperationException();
	}

	private IObjRef getPrimaryOri(IObjRef ori)
	{
		IObjRef primaryOri;
		if (ori.getIdNameIndex() == -1)
		{
			primaryOri = ori;
		}
		else
		{
			primaryOri = new ObjRef(ori.getRealType(), stringToNumber((String) ori.getId()), (short) 1);
		}
		return primaryOri;
	}

	private String getName(IObjRef ori)
	{
		String name;
		if (ori.getIdNameIndex() == -1)
		{
			name = numberToString((Integer) ori.getId());
		}
		else
		{
			name = (String) ori.getId();
		}
		return name;
	}

	protected IObjRef[] getParentRelation(IObjRef primaryOri, Integer checksum)
	{
		IObjRef[] parentRelation;
		if (checksum.equals(primaryOri.getId()))
		{
			parentRelation = new IObjRef[] {};
		}
		else
		{
			parentRelation = new IObjRef[] { new ObjRef(ExternalEntity.class, checksum, null) };
		}
		return parentRelation;
	}

	protected IObjRef[][] getRelations(IEntityMetaData metaData, IObjRef[] parentRelation)
	{
		IObjRef[][] relations = new IObjRef[metaData.getRelationMembers().length][];
		if (metaData.getEntityType().equals(ExternalEntity2.class))
		{
			IObjRef refToLocal = objRefFactory.createObjRef(LocalEntity.class, 0, "LocalEntity 893", null);

			relations[metaData.getIndexByRelationName("Parent")] = parentRelation;
			relations[metaData.getIndexByRelationName("Local")] = new IObjRef[] { refToLocal };
		}
		else
		{
			relations[metaData.getIndexByRelationName("Parent")] = parentRelation;
		}
		return relations;
	}

	private String numberToString(Integer id)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		boolean notFirst = false;
		int value = id;
		int tenth = value / 10;

		while (value > 0)
		{
			if (notFirst)
			{
				sb.insert(0, ' ');
			}
			notFirst = true;

			int digit = value - tenth * 10;
			sb.insert(0, toString.get(digit));

			value = tenth;
			tenth = value / 10;
		}

		return sb.toString();
	}

	private Integer stringToNumber(String id)
	{
		Integer number = Integer.valueOf(0);
		int factor = 1;
		String[] parts = id.split(" ");

		for (int i = parts.length; i-- > 0;)
		{
			number += toString.indexOf(parts[i]) * factor;
			factor *= 10;
		}

		return number;
	}

	private int checksum(Integer id)
	{
		int value = id;
		int tenth = value / 10;
		int checksum = 0;

		while (value > 0)
		{
			checksum += value - tenth * 10;

			value = tenth;
			tenth = value / 10;
		}

		return checksum;
	}

	private void debugToLoad(List<IObjRef> orisToLoad)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();

		StringBuilder sb = current.create(StringBuilder.class);
		try
		{
			int count = orisToLoad.size();
			sb.append("List<IObjRef> : ").append(count).append(" item");
			if (count != 1)
			{
				sb.append('s');
			}
			sb.append(" [");

			for (int a = orisToLoad.size(); a-- > 0;)
			{
				IObjRef oriToLoad = orisToLoad.get(a);
				if (count > 1)
				{
					sb.append("\r\n\t");
				}
				if (oriToLoad instanceof IPrintable)
				{
					((IPrintable) oriToLoad).toString(sb);
				}
				else
				{
					sb.append(oriToLoad.toString());
				}
			}
			sb.append("]");

			log.debug(sb.toString());
		}
		finally
		{
			current.dispose(sb);
		}
	}
}
