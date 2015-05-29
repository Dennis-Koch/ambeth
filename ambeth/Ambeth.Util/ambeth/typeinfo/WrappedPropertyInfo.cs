using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class WrappedPropertyInfo : AbstractPropertyInfo, IPropertyInfoIntern, IPrintable
    {        
        protected readonly PropertyInfo propertyInfo;

        public WrappedPropertyInfo(Type entityType, PropertyInfo propertyInfo) : base(entityType)
        {
            EntityType = entityType;
            Name = propertyInfo.Name;
            DeclaringType = propertyInfo.DeclaringType;
            PropertyType = propertyInfo.PropertyType;
            ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(PropertyType, null);

            this.propertyInfo = propertyInfo;

            IsReadable = propertyInfo.GetGetMethod() != null ? propertyInfo.GetGetMethod().IsPublic || propertyInfo.GetGetMethod().IsFamily: false;
            IsWritable = propertyInfo.GetSetMethod() != null ? propertyInfo.GetSetMethod().IsPublic || propertyInfo.GetSetMethod().IsFamily : false;
			IsFieldWritable = IsWritable || BackingField != null && !BackingField.IsInitOnly;
			
            Init();
        }

        protected override void Init()
        {
            PutAnnotations(propertyInfo);
            base.Init();
        }

        public override Object GetValue(Object obj)
        {
            return propertyInfo.GetValue(obj, null);
        }

        public override void SetValue(Object obj, Object value)
        {
            propertyInfo.SetValue(obj, value, null);
        }
    }
}