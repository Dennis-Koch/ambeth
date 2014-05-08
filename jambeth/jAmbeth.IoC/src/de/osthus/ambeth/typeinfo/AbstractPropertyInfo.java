package de.osthus.ambeth.typeinfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public abstract class AbstractPropertyInfo implements IPropertyInfoIntern, IPrintable
{
	protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

	protected ILinkedMap<Class<? extends Annotation>, Annotation> annotations;
	protected Class<?> entityType;
	protected String name;
	protected Class<?> propertyType;
	protected Class<?> elementType;
	protected Field backingField;
	protected Class<?> declaringType;
	protected int modifiers;

	public AbstractPropertyInfo(Class<?> entityType)
	{
		this(entityType, null);
	}

	public AbstractPropertyInfo(Class<?> entityType, IThreadLocalObjectCollector objectCollector)
	{
		this.entityType = entityType;
	}

	protected void init(IThreadLocalObjectCollector objectCollector)
	{
		if (backingField != null)
		{
			modifiers = backingField.getModifiers();
		}
		ParamChecker.assertNotNull(this.entityType, "entityType");
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(declaringType, "declaringType");
		ParamChecker.assertNotNull(elementType, "elementType");
		ParamChecker.assertNotNull(propertyType, "propertyType");
	}

	protected void putAnnotations(AccessibleObject obj)
	{
		if (obj instanceof Method)
		{
			Class<?> baseType = ((Method) obj).getDeclaringClass().getSuperclass();
			Method overriddenMethod = baseType != null ? ReflectUtil.getDeclaredMethod(true, baseType, ((Method) obj).getName(),
					((Method) obj).getParameterTypes()) : null;
			if (overriddenMethod != null)
			{
				putAnnotations(overriddenMethod);
			}
		}
		Annotation[] annotations = obj.getAnnotations();
		for (Annotation annotation : annotations)
		{
			Class<? extends Annotation> type = annotation.annotationType();
			if (this.annotations == null)
			{
				this.annotations = new LinkedHashMap<Class<? extends Annotation>, Annotation>();
			}
			this.annotations.putIfNotExists(type, annotation);
		}
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getModifiers()
	{
		return modifiers;
	}

	@Override
	public Class<?> getPropertyType()
	{
		return propertyType;
	}

	@Override
	public Field getBackingField()
	{
		return backingField;
	}

	@Override
	public Annotation[] getAnnotations()
	{
		ILinkedMap<Class<? extends Annotation>, Annotation> annotations = this.annotations;
		if (annotations == null)
		{
			return EMPTY_ANNOTATIONS;
		}
		IList<Annotation> values = annotations.values();
		return values.toArray(Annotation.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		IMap<Class<? extends Annotation>, Annotation> annotations = this.annotations;
		if (annotations == null)
		{
			return null;
		}
		return (V) annotations.get(annotationType);
	}

	@Override
	public <V extends Annotation> boolean isAnnotationPresent(Class<V> annotationType)
	{
		IMap<Class<? extends Annotation>, Annotation> annotations = this.annotations;
		if (annotations == null)
		{
			return false;
		}
		return annotations.containsKey(annotationType);
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return declaringType;
	}

	@Override
	public Class<?> getElementType()
	{
		return elementType;
	}

	@Override
	public void setElementType(Class<?> elementType)
	{
		this.elementType = elementType;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(declaringType.getName()).append('.').append(getName());
	}
}
