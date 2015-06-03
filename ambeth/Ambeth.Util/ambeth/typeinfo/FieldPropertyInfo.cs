using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class FieldPropertyInfo : AbstractPropertyInfo
    {
        public FieldPropertyInfo(Type entityType, String propertyName, FieldInfo field)
            : base(entityType)
        {
            EntityType = entityType;
            BackingField = field;
			AddModifiers(field);
            Name = propertyName;
            DeclaringType = field.DeclaringType;
            PropertyType = field.FieldType;
            ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(PropertyType, null);

            IsWritable = field.IsPublic || field.IsFamily;
            IsReadable = IsWritable;

            Init();
        }

        protected override void Init()
        {
            PutAnnotations(BackingField);

            base.Init();
        }

        public override Object GetValue(Object obj)
        {
            return BackingField.GetValue(obj);
        }

        public override void SetValue(Object obj, Object value)
        {
            BackingField.SetValue(obj, value);
        }
    }
}
