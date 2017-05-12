using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Mapping
{
    public interface IMapperService : IDisposable
    {
        Object MapToBusinessObject(Object valueObject);

        Object GetMappedBusinessObject(IObjRef objRef);

        IList<Object> MapToBusinessObjectListFromListType(Object listTypeObject);

        IList<Object> MapToBusinessObjectList(IList<Object> valueObjectList);

        Object MapToValueObject(Object businessObject, Type valueObjectType);

        Object MapToValueObjectListType(IList<Object> businessObjectList, Type valueObjectType, Type valueObjectListType);

        Object MapToValueObjectRefListType(IList<Object> businessObjectList, Type valueObjectRefListType);

        IList<Object> MapToValueObjectList(IList<Object> businessObjectList, Type valueObjectType);

        Object GetIdFromValueObject(Object valueObject);

        Object GetVersionFromValueObject(Object valueObject);

        IList<Object> GetAllActiveBusinessObjects();
    }
}
