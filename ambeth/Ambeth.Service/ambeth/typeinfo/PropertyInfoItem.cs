using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.Serialization;
using System.Xml.Serialization;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class PropertyInfoItem : RelationInfoItem
    {
        protected IPropertyInfoIntern property;

        protected String xmlName;

        protected bool xmlIgnore;

        public bool AllowNullEquivalentValue { get; set; }
        
        public PropertyInfoItem(IPropertyInfo property) : this(property, true)
        {
            // Intended blank
        }

        public PropertyInfoItem(IPropertyInfo property, bool allowNullEquivalentValue)
        {
            this.property = (IPropertyInfoIntern)property;
            this.AllowNullEquivalentValue = allowNullEquivalentValue;
            Type propertyType = property.PropertyType;
            DeclaringType = property.DeclaringType;
            ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(propertyType, null);

            if (propertyType.IsValueType || propertyType.IsPrimitive)
            {
                NullEquivalentValue = NullEquivalentValueUtil.GetNullEquivalentValue(propertyType);
            }
            XmlElementAttribute annotation = property.GetAnnotation<XmlElementAttribute>();
            if (annotation != null)
            {
                XMLName = annotation.ElementName;
            }
            if (XMLName == null || XMLName.Length == 0)
            {
                XMLName = Name;
            }
            xmlIgnore = false;
#if !SILVERLIGHT
            if (property.GetAnnotation<NonSerializedAttribute>() != null)
            {
                xmlIgnore = true;
            }
#endif
            if (property.GetAnnotation<IgnoreDataMemberAttribute>() != null)
            {
                xmlIgnore = true;
            }
            if (property.GetAnnotation<XmlIgnoreAttribute>() != null)
            {
                xmlIgnore = true;
            }
            if (!CanRead || !CanWrite)
            {
                xmlIgnore = true;
            }
        }

        public IPropertyInfo Property
        {
            get
            {
                return property;
            }
        }

        public void SetProperty(IPropertyInfoIntern property)
        {
            this.property = property;
        }

        public override Type RealType
        {
            get
            {
                return property.PropertyType;
            }
        }

        public override bool CanRead
        {
            get
            {
                return property.IsReadable;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return property.IsWritable;
            }
        }

        public override void SetValue(Object obj, Object value)
        {
            if (value == null && AllowNullEquivalentValue)
            {
                value = NullEquivalentValue;
            }
            property.SetValue(obj, value);
        }

        public override Object GetValue(Object obj)
        {
            return GetValue(obj, AllowNullEquivalentValue);
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            Object value = property.GetValue(obj);
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
            return property.GetAnnotation<V>();
        }

        public override String Name
        {
            get
            {
                return property.Name;
            }
        }

        public override bool IsXMLIgnore
        {
            get { return xmlIgnore; }
        }

        public override String ToString()
        {
            return "Property " + Name + "/" + XMLName;
        }
    }
}