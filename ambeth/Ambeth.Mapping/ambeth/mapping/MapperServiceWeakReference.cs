using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Mapping
{
    public class MapperServiceWeakReference : IMapperService
    {
        protected IMapperService mapperService;

        public MapperServiceWeakReference(IMapperService mapperService)
        {
            this.mapperService = mapperService;
        }

        ~MapperServiceWeakReference()
        {
            if (mapperService != null)
            {
                mapperService.Dispose();
            }
        }

        public void Dispose()
        {
            if (mapperService != null)
            {
                mapperService.Dispose();
                mapperService = null;
            }
        }

        public Object MapToBusinessObject(Object valueObject)
        {
            return mapperService.MapToBusinessObject(valueObject);
        }

        public IList<Object> MapToBusinessObjectListFromListType(Object listTypeObject)
        {
            return mapperService.MapToBusinessObjectListFromListType(listTypeObject);
        }

        public IList<Object> MapToBusinessObjectList(IList<Object> valueObjectList)
        {
            return mapperService.MapToBusinessObjectList(valueObjectList);
        }

        public Object MapToValueObject(Object businessObject, Type valueObjectType)
        {
            return mapperService.MapToValueObject(businessObject, valueObjectType);
        }

        public Object MapToValueObjectListType(IList<Object> businessObjectList, Type valueObjectType, Type valueObjectListType)
        {
            return mapperService.MapToValueObjectListType(businessObjectList, valueObjectType, valueObjectListType);
        }

        public object MapToValueObjectRefListType(IList<Object> businessObjectList, Type valueObjectRefListType)
        {
            return mapperService.MapToValueObjectRefListType(businessObjectList, valueObjectRefListType);
        }

        public IList<Object> MapToValueObjectList(IList<Object> businessObjectList, Type valueObjectType)
        {
            return mapperService.MapToValueObjectList(businessObjectList, valueObjectType);
        }

        public Object GetIdFromValueObject(Object valueObject)
        {
            return mapperService.GetIdFromValueObject(valueObject);
        }

        public Object GetVersionFromValueObject(Object valueObject)
        {
            return mapperService.GetVersionFromValueObject(valueObject);
        }

        public Object GetMappedBusinessObject(IObjRef objRef)
        {
            return mapperService.GetMappedBusinessObject(objRef);
        }

        public IList<Object> GetAllActiveBusinessObjects()
        {
            return mapperService.GetAllActiveBusinessObjects();
        }
    }
}
