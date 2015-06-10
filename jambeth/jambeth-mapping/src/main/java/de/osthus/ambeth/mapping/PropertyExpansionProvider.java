package de.osthus.ambeth.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyExpansionProvider implements IPropertyExpansionProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	IEntityMetaDataProvider entityMetaDataProvider;

	Tuple2KeyHashMap<Class<?>, String, PropertyExpansion> propertyExpansionCache = new Tuple2KeyHashMap<Class<?>, String, PropertyExpansion>();

	@Override
	public PropertyExpansion getPropertyExpansion(Class<?> entityType, String propertyPath)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(propertyPath, "propertyPath");

		PropertyExpansion propertyExpansion = propertyExpansionCache.get(entityType, propertyPath);
		if (propertyExpansion == null)
		{
			propertyExpansion = getPropertyExpansionIntern(entityType, propertyPath);
			propertyExpansionCache.put(entityType, propertyPath, propertyExpansion);
		}

		return propertyExpansion;
	}

	protected PropertyExpansion getPropertyExpansionIntern(Class<?> entityType, String propertyPath)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		if (entityType == null)
		{
			return null;
		}

		ArrayList<Member> memberPath = new ArrayList<Member>();
		ArrayList<IEntityMetaData> metaDataPath = new ArrayList<IEntityMetaData>();
		Class<?> lastType = null;
		for (String pathToken : getPath(propertyPath))
		{
			if (metaData == null)
			{
				throw new IllegalArgumentException("Could not find metaData for:" + (lastType == null ? "null" : lastType.toString()));
			}
			Member member = metaData.getMemberByName(pathToken);
			if (member == null)
			{
				throw new IllegalArgumentException("The provided propertyPath can not be resolved. Check: " + pathToken
						+ " (Hint: propertyNames need to start with an UpperCase letter!)");
			}
			memberPath.add(member);
			// get next metaData
			metaData = entityMetaDataProvider.getMetaData(member.getRealType(), true);
			metaDataPath.add(metaData);
			lastType = member.getRealType();

		}

		PropertyExpansion propertyExpansion = new PropertyExpansion(memberPath, metaDataPath);
		return propertyExpansion;
	}

	protected List<String> getPath(String propertyPath)
	{
		String[] pathTokens = propertyPath.split("\\.");
		if (pathTokens != null)
		{
			return Arrays.asList(pathTokens);
		}

		return Collections.emptyList();
	}
}
