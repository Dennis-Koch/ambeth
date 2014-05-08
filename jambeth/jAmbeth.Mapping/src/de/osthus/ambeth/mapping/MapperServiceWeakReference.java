package de.osthus.ambeth.mapping;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;

public class MapperServiceWeakReference implements IMapperService
{
	protected IMapperService mapperService;

	protected MapperServiceWeakReference(IMapperService mapperService)
	{
		this.mapperService = mapperService;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (mapperService != null)
		{
			mapperService.dispose();
		}
	}

	@Override
	public void dispose()
	{
		if (mapperService != null)
		{
			mapperService.dispose();
			mapperService = null;
		}
	}

	@Override
	public <T> T mapToBusinessObject(Object sourceValueObject)
	{
		return mapperService.mapToBusinessObject(sourceValueObject);
	}

	@Override
	public <T> T mapToBusinessObjectListFromListType(Object listTypeObject)
	{
		return mapperService.mapToBusinessObjectListFromListType(listTypeObject);
	}

	@Override
	public <T> T mapToBusinessObjectList(List<?> valueObjectList)
	{
		return mapperService.mapToBusinessObjectList(valueObjectList);
	}

	@Override
	public <T> T mapToValueObject(Object sourceBusinessObject, Class<T> valueObjectType)
	{
		return mapperService.mapToValueObject(sourceBusinessObject, valueObjectType);
	}

	@Override
	public <L> L mapToValueObjectListType(List<?> businessObject, Class<?> valueObjectType, Class<L> listType)
	{
		return mapperService.mapToValueObjectListType(businessObject, valueObjectType, listType);
	}

	@Override
	public <L> L mapToValueObjectRefListType(List<?> businessObjectList, Class<L> listType)
	{
		return mapperService.mapToValueObjectRefListType(businessObjectList, listType);
	}

	@Override
	public <T> T mapToValueObjectList(List<?> businessObjectList, Class<?> valueObjectType)
	{
		return mapperService.mapToValueObjectList(businessObjectList, valueObjectType);
	}

	@Override
	public Object getIdFromValueObject(Object valueObject)
	{
		return mapperService.getIdFromValueObject(valueObject);
	}

	@Override
	public Object getVersionFromValueObject(Object valueObject)
	{
		return mapperService.getIdFromValueObject(valueObject);
	}

	@Override
	public Object getMappedBusinessObject(IObjRef objRef)
	{
		return mapperService.getMappedBusinessObject(objRef);
	}

	@Override
	public IList<Object> getAllActiveBusinessObjects()
	{
		return mapperService.getAllActiveBusinessObjects();
	}
}
