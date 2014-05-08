using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Runtime.Serialization;
using System.Xml.Serialization;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class FieldInfoItem : RelationInfoItem
    {
        protected FieldInfo field;

        protected String propertyName;

        protected String xmlName;
        
        protected bool xmlIgnore;

        protected bool allowNullEquivalentValue;
        
        public FieldInfoItem(FieldInfo field)
            : this(field, true)
        {
            // Intended blank
        }

        public FieldInfoItem(FieldInfo field, bool allowNullEquivalentValue)
            : this(field, allowNullEquivalentValue, field.Name)
        {
            // Intended blank
        }

        public FieldInfoItem(FieldInfo field, String propertyName)
            : this(field, true, propertyName)
        {
            // Intended blank
        }

        public FieldInfoItem(FieldInfo field, bool allowNullEquivalentValue, String propertyName)
        {
            ParamChecker.AssertParamNotNull(field, "field");
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            this.allowNullEquivalentValue = allowNullEquivalentValue;
            this.field = field;
            DeclaringType = field.DeclaringType;
#if SILVERLIGHT
            if (!field.IsPublic)
            {
                throw new Exception("Field '" + field + "' not public. This is not valid in Silverlight. One possibility is to set the field public or define public setter/getter.");
            }
#endif
            this.propertyName = propertyName;

            Type fieldType = field.FieldType;
            ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(fieldType, null);
            if (fieldType.IsValueType || fieldType.IsPrimitive)
            {
                NullEquivalentValue = NullEquivalentValueUtil.GetNullEquivalentValue(fieldType);
            }
            Object[] attributes = field.GetCustomAttributes(typeof(XmlElementAttribute), false);
            if (attributes != null && attributes.Length > 0)
            {
                XMLName = ((XmlElementAttribute)attributes[0]).ElementName;
            }
            if (XMLName == null || XMLName.Length == 0)
            {
                XMLName = Name;
            }
            xmlIgnore = false;
#if !SILVERLIGHT
            attributes = field.GetCustomAttributes(typeof(NonSerializedAttribute), false);
            if (attributes != null && attributes.Length > 0)
            {
                xmlIgnore = true;
            }
#endif
            attributes = field.GetCustomAttributes(typeof(IgnoreDataMemberAttribute), false);
            if (attributes != null && attributes.Length > 0)
            {
                xmlIgnore = true;
            }
            attributes = field.GetCustomAttributes(typeof(XmlIgnoreAttribute), false);
            if (attributes != null && attributes.Length > 0)
            {
                xmlIgnore = true;
            }
        }
        
        public FieldInfo Field
        {
            get
            {
                return field;
            }
        }

        public override Type RealType
        {
            get
            {
                return field.FieldType;
            }
        }

        public override void SetValue(Object obj, Object value)
        {
            if (value == null && allowNullEquivalentValue)
            {
                value = NullEquivalentValue;
            }
            field.SetValue(obj, value);
        }

        public override Object GetValue(Object obj)
        {
            return GetValue(obj, allowNullEquivalentValue);
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            Object value = field.GetValue(obj);
            Object nullEquivalentValue = this.NullEquivalentValue;
            if (nullEquivalentValue != null && nullEquivalentValue.Equals(value))
            {
                if (allowNullEquivalentValue)
                {
                    return nullEquivalentValue;
                }
                return null;
            }
            return value;
        }

        public override V GetAnnotation<V>()
        {
            Object[] attributes = field.GetCustomAttributes(typeof(V), true);
            if (attributes.Length > 0)
            {
                return (V)attributes[0];
            }
            return default(V);
        }

        public override String Name
        {
            get
            {
                return propertyName;
            }
        }

        public override bool IsXMLIgnore
        {
            get { return xmlIgnore; }
        }

        public override String ToString()
        {
            return "Field " + Name + "/" + XMLName;
        }
    }
}
