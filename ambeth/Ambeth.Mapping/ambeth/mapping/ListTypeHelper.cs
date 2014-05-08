using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Mapping
{
    public class ListTypeHelper : IListTypeHelper, IInitializingBean
    {
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(TypeInfoProvider, "TypeInfoProvider");
        }

        public Object PackInListType(IEnumerable referencedVOs, Type listType)
        {
            Object listTypeInst = Activator.CreateInstance(listType);

            if (referencedVOs == null)
            {
                return listType;
            }

            ITypeInfoItem accessor = GetListTypeAccessor(listType);
            if (accessor.CanWrite)
            {
                if (!accessor.RealType.IsAssignableFrom(referencedVOs.GetType()))
                {
                    ICollection<Object> targetCollection;
                    Type propertyType = accessor.RealType;
                    if (typeof(IList).IsAssignableFrom(propertyType))
                    {
                        targetCollection = new List<Object>();
                        foreach (Object item in referencedVOs)
                        {
                            targetCollection.Add(item);
                        }
                    }
                    // TODO type of Set
                    //else if (typeof(ISet).IsAssignableFrom(propertyType))
                    //{
                    //    targetCollection = new HashSet<Object>(referencedVOs);
                    //}
                    else
                    {
                        throw new ArgumentException("Collection type of '" + propertyType.Name + "' is not supported");
                    }
                    referencedVOs = targetCollection;
                }
                accessor.SetValue(listTypeInst, referencedVOs);
            }
            else
            {
                IList internalList = (IList)accessor.GetValue(listTypeInst);
                IEnumerator enumerator = referencedVOs.GetEnumerator();
                while (enumerator.MoveNext())
                {
                    Object entry = enumerator.Current;
                    internalList.Add(entry);
                }
            }

            return listTypeInst;
        }

        public Object UnpackListType(Object item)
        {
            ITypeInfoItem accessor = GetListTypeAccessor(item.GetType());
            return accessor.GetValue(item);
        }

        public bool IsListType(Type type)
        {
            PropertyInfo[] properties = type.GetProperties();
            return properties.Length == 1;
        }

        protected ITypeInfoItem GetListTypeAccessor(Type type)
        {
            PropertyInfo[] properties = type.GetProperties();
            if (properties.Length != 1)
            {
                throw new ArgumentException("ListTypes must have exactly one property: '" + type + "'");
            }
            return TypeInfoProvider.GetMember(type, properties[0]);
        }
    }
}
