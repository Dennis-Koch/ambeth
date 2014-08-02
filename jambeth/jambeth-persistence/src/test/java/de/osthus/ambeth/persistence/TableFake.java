package de.osthus.ambeth.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

public class TableFake extends Table
{
	public int nextId = 1;
	public Map<Object, Map<String, Object>> content = new HashMap<Object, Map<String, Object>>();

	public TableFake(String name, Class<?> entityType, IMemberTypeProvider memberTypeProvider, IAlreadyLinkedCache alreadyLinkedCache,
			IThreadLocalObjectCollector objectCollector, IEntityMetaDataProvider entityMetaDataProvider)
	{
		Field idField = new Field();
		idField.setTable(this);
		idField.setName("ID");
		idField.setFieldType(Long.class);
		idField.setMember(entityMetaDataProvider.getMetaData(entityType).getIdMember());
		setIdField(idField);
		Field versionField = new Field();
		versionField.setTable(this);
		versionField.setName("VERSION");
		versionField.setFieldType(Long.class);
		versionField.setMember(entityMetaDataProvider.getMetaData(entityType).getVersionMember());
		setVersionField(versionField);

		setName(name);
		setEntityType(entityType);
		this.memberTypeProvider = memberTypeProvider;
		this.alreadyLinkedCache = alreadyLinkedCache;
		this.objectCollector = objectCollector;
	}

	@Override
	public IList<Object> acquireIds(int count)
	{
		IList<Object> ids = new de.osthus.ambeth.collections.ArrayList<Object>(count);
		for (int i = count; i-- > 0;)
		{
			ids.add(this.nextId++);
		}
		return ids;
	}

	@Override
	public Object insert(Object id, IParamHolder<Object> newId, ILinkedMap<String, Object> puis)
	{
		ParamChecker.assertParamNotNull(id, "id");
		ParamChecker.assertParamNotNull(newId, "newId");

		newId.setValue(id);
		Integer version;
		Map<String, Object> obj = this.content.get(id);
		if (obj != null)
		{
			version = (Integer) obj.get("version");
		}
		else
		{
			version = 0;
			obj = new HashMap<String, Object>();
			obj.put("id", id);
			obj.put("version", version);
			this.content.put(id, obj);
		}

		return update(id, version, puis);
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<String, Object> puis)
	{
		return update(id, version, puis, null);
	}

	public Object update(Object id, Object version, ILinkedMap<String, Object> puis, ILinkedMap<String, IObjRef[][]> ruis)
	{
		Integer newVersion = ((Integer) version) + 1;

		Map<String, Object> obj = this.content.get(id);
		if (obj != null && ((Integer) obj.get("version")).equals(version))
		{
			obj.put("version", newVersion);
			if (puis != null)
			{
				Iterator<String> memberNames = puis.keySet().iterator();
				while (memberNames.hasNext())
				{
					String memberName = memberNames.next();
					obj.put(memberName, puis.get(memberName));
				}
			}
			if (ruis != null)
			{
				Iterator<String> memberNames = ruis.keySet().iterator();
				while (memberNames.hasNext())
				{
					String memberName = memberNames.next();
					@SuppressWarnings("unchecked")
					List<IObjRef> current = (List<IObjRef>) obj.get(memberName);
					IObjRef[] addedORIs = ruis.get(memberName)[0];
					IObjRef[] removedORIs = ruis.get(memberName)[1];
					if (current == null)
					{
						current = new ArrayList<IObjRef>(addedORIs.length);
						obj.put(memberName, current);
					}
					current.addAll(Arrays.asList(addedORIs));
					current.removeAll(Arrays.asList(removedORIs));
				}
			}
		}

		return newVersion;
	}

	@Override
	public void delete(List<IObjRef> oris)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
