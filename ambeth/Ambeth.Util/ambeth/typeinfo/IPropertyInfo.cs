using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface IPropertyInfo
    {
        Type EntityType { get; }

    	String Name { get; }

	    Type PropertyType { get; }

	    Type DeclaringType { get; }

	    Type ElementType { get; }

	    bool IsReadable { get; }

	    bool IsWritable { get; }
		
	    FieldInfo BackingField { get; }

        int Modifiers { get; }

    	Object GetValue(Object obj);

	    void SetValue(Object obj, Object value);

	    Attribute[] GetAnnotations();

        V GetAnnotation<V>() where V : Attribute;

        Attribute GetAnnotation(Type annotationType);

        bool IsAnnotationPresent<V>() where V : Attribute;

        bool IsAnnotationPresent(Type annotationType);
    }
}
