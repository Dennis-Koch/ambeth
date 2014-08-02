package de.osthus.ambeth.bytecode.visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.MemberTypeProvider;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.typeinfo.FieldPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;

public class EntityMetaDataMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_canRead = new MethodInstance(null, Member.class, boolean.class, "canRead");

	protected static final MethodInstance template_m_canWrite = new MethodInstance(null, Member.class, boolean.class, "canWrite");

	protected static final MethodInstance template_m_getAnnotation = new MethodInstance(null, Member.class, Annotation.class, "getAnnotation", Class.class);

	protected static final MethodInstance template_m_getDeclaringType = new MethodInstance(null, Member.class, Class.class, "getDeclaringType");

	protected static final MethodInstance template_m_getNullEquivalentValue = new MethodInstance(null, Member.class, Object.class, "getNullEquivalentValue");

	protected static final MethodInstance template_m_getName = new MethodInstance(null, Member.class, String.class, "getName");

	protected static final MethodInstance template_m_getRealType = new MethodInstance(null, Member.class, Class.class, "getRealType");

	protected static final MethodInstance template_m_getValue = new MethodInstance(null, Member.class, Object.class, "getValue", Object.class);

	protected static final MethodInstance template_m_getValueWithFlag = new MethodInstance(null, Member.class, Object.class, "getValue", Object.class,
			boolean.class);

	protected static final MethodInstance template_m_setValue = new MethodInstance(null, Member.class, void.class, "setValue", Object.class, Object.class);

	protected final Class<?> entityType;

	protected final String memberName;

	protected IBytecodeEnhancer bytecodeEnhancer;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IPropertyInfoProvider propertyInfoProvider;

	public EntityMetaDataMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IBytecodeEnhancer bytecodeEnhancer,
			IEntityMetaDataProvider entityMetaDataProvider, IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.entityType = entityType;
		this.memberName = memberName;
		this.entityMetaDataProvider = entityMetaDataProvider;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		IPropertyInfo[] propertyPath = MemberTypeProvider.buildPropertyPath(entityType, memberName, propertyInfoProvider);
		implementCanRead(propertyPath);
		implementCanWrite(propertyPath);
		implementGetAnnotation(propertyPath);
		implementGetDeclaringType(propertyPath);
		implementGetName(propertyPath);
		implementGetNullEquivalentValue(propertyPath);
		implementGetRealType(propertyPath);
		implementGetValue(propertyPath);
		implementSetValue(propertyPath);
		super.visitEnd();
	}

	protected void implementCanRead(IPropertyInfo[] property)
	{
		MethodGenerator mv = visitMethod(template_m_canRead);
		mv.push(property[property.length - 1].isReadable());
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementCanWrite(IPropertyInfo[] property)
	{
		MethodGenerator mv = visitMethod(template_m_canWrite);
		mv.push(property[property.length - 1].isWritable());
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetNullEquivalentValue(IPropertyInfo[] property)
	{
		MethodGenerator mv = visitMethod(template_m_getNullEquivalentValue);
		Class<?> propertyType = property[property.length - 1].getPropertyType();
		mv.pushNullOrZero(propertyType);
		if (propertyType.isPrimitive())
		{
			mv.box(Type.getType(propertyType));
		}
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetRealType(IPropertyInfo[] property)
	{
		MethodGenerator mv = visitMethod(template_m_getRealType);
		mv.push(property[property.length - 1].getPropertyType());
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetAnnotation(IPropertyInfo[] property)
	{
		HashMap<Class<?>, Annotation> typeToAnnotationMap = new HashMap<Class<?>, Annotation>();
		Annotation[] annotations = property[property.length - 1].getAnnotations();
		for (Annotation annotation : annotations)
		{
			typeToAnnotationMap.put(annotation.getClass(), annotation);
		}
		FieldInstance f_typeToAnnotationMap = implementStaticAssignedField("typeToAnnotationMap", typeToAnnotationMap);
		MethodGenerator mv = visitMethod(template_m_getAnnotation);
		mv.getThisField(f_typeToAnnotationMap);
		mv.loadArg(0);
		mv.invokeVirtual(new MethodInstance(null, HashMap.class, Object.class, "get", Object.class));
		mv.checkCast(Annotation.class);
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetDeclaringType(IPropertyInfo[] property)
	{
		MethodGenerator mv = visitMethod(template_m_getDeclaringType);
		mv.push(entityType);
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetName(IPropertyInfo[] property)
	{
		StringBuilder compositeName = new StringBuilder();
		for (int a = 0, size = property.length; a < size; a++)
		{
			if (a > 0)
			{
				compositeName.append('.');
			}
			compositeName.append(property[a].getName());
		}
		MethodGenerator mv = visitMethod(template_m_getName);
		mv.push(compositeName.toString());
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementGetValue(IPropertyInfo[] propertyPath)
	{
		{
			MethodGenerator mv = visitMethod(template_m_getValue);
			mv.loadThis();
			mv.loadArg(0);
			mv.push(false);
			mv.invokeVirtual(template_m_getValueWithFlag);
			mv.returnValue();
			mv.endMethod();
		}

		MethodGenerator mv = visitMethod(template_m_getValueWithFlag);

		for (int a = 0, size = propertyPath.length; a < size; a++)
		{
			IPropertyInfo property = propertyPath[a];
			if (property instanceof MethodPropertyInfo && ((MethodPropertyInfo) property).getGetter() == null)
			{
				throw new IllegalStateException("Property not readable: " + property.getEntityType().getName() + "." + property.getName());
			}
		}
		// IEntityMetaData metaDataOfProperty = entityMetaDataProvider.getMetaData(propertyPath[0].getEntityType(), true);
		// if (metaDataOfProperty != null)
		// {
		// if (metaDataOfProperty.getEnhancedType() == null)
		// {
		// }
		// }
		// Class<?> declaringType = metaDataOfProperty != null ? metaDataOfProperty.getEnhancedType() : propertyPath[0].getEntityType();
		Class<?> declaringType = propertyPath[0].getEntityType();
		Label l_finish = mv.newLabel();
		mv.loadArg(0);
		mv.checkCast(declaringType);
		for (int a = 0, size = propertyPath.length - 1; a < size; a++)
		{
			invokeGetProperty(mv, propertyPath[a]);
			mv.dup();
			mv.ifNull(l_finish);
		}
		IPropertyInfo lastProperty = propertyPath[propertyPath.length - 1];
		invokeGetProperty(mv, lastProperty);
		if (lastProperty.getPropertyType().isPrimitive())
		{
			Type pType = Type.getType(lastProperty.getPropertyType());
			int loc_value = mv.newLocal(pType);
			mv.storeLocal(loc_value);
			mv.loadLocal(loc_value);
			Label l_valueIsNonZero = mv.newLabel();
			Label l_nullAllowed = mv.newLabel();

			mv.ifZCmp(pType, GeneratorAdapter.NE, l_valueIsNonZero);

			// check null-equi flag
			mv.loadArg(1);
			mv.ifZCmp(GeneratorAdapter.EQ, l_nullAllowed);
			mv.pushNullOrZero(pType);
			mv.box(pType);
			mv.returnValue();

			mv.mark(l_nullAllowed);
			mv.pushNull();
			mv.returnValue();

			mv.mark(l_valueIsNonZero);
			mv.loadLocal(loc_value);
			mv.valueOf(pType);
		}
		mv.mark(l_finish);
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementSetValue(IPropertyInfo[] propertyPath)
	{
		MethodGenerator mv = visitMethod(template_m_setValue);

		for (int a = 0, size = propertyPath.length - 1; a < size; a++)
		{
			IPropertyInfo property = propertyPath[a];
			if (property instanceof MethodPropertyInfo && ((MethodPropertyInfo) property).getGetter() == null)
			{
				throw new IllegalStateException("Property not readable: " + property.getEntityType().getName() + "." + property.getName());
			}
		}
		IPropertyInfo lastProperty = propertyPath[propertyPath.length - 1];
		if (lastProperty instanceof MethodPropertyInfo && ((MethodPropertyInfo) lastProperty).getSetter() == null)
		{
			throw new IllegalStateException("Property not writable: " + lastProperty.getEntityType().getName() + "." + lastProperty.getName());
		}
		mv.loadArg(0);
		mv.checkCast(propertyPath[0].getEntityType());

		for (int a = 0, size = propertyPath.length - 1; a < size; a++)
		{
			invokeGetProperty(mv, propertyPath[a]);
			// mv.dup();
			// Label l_pathIsNonNull = mv.newLabel();
			// mv.ifNonNull(l_pathIsNonNull);
			//
			// mv.pop(); // remove remaining null of embedded object from stack, now the cached parent object is on the stack
			// mv.dup(); // cache parent object again
			//
			// mv.callThisGetter(p_embeddedMemberTemplate);
			// mv.push(propertyPath[a].getPropertyType()); // embeddedType
			// mv.push(entityType); // entityType
			// mv.// parentObject
			// mv.push(sb.toString()); // memberPath
			// mv.invokeVirtual(template_m_createEmbeddedObject);
			//
			// mv.mark(l_pathIsNonNull);
		}

		mv.loadArg(1);
		Type lastPropertyType = Type.getType(lastProperty.getPropertyType());
		if (lastProperty.getPropertyType().isPrimitive())
		{
			Type pType = Type.getType(lastProperty.getPropertyType());
			Label l_valueIsNonNull = mv.newLabel();
			Label l_valueIsValid = mv.newLabel();

			mv.ifNonNull(l_valueIsNonNull);
			mv.pushNullOrZero(pType);
			mv.goTo(l_valueIsValid);

			mv.mark(l_valueIsNonNull);
			mv.loadArg(1);
			mv.unbox(pType);
			mv.mark(l_valueIsValid);
		}
		else
		{
			mv.checkCast(lastPropertyType);
		}
		invokeSetProperty(mv, lastProperty);
		mv.returnValue();

		mv.endMethod();
	}

	protected void invokeGetProperty(MethodGenerator mv, IPropertyInfo property)
	{
		if (property instanceof MethodPropertyInfo)
		{
			Method method = ((MethodPropertyInfo) property).getGetter();
			if (method.getDeclaringClass().isInterface())
			{
				mv.invokeInterface(new MethodInstance(method));
			}
			else
			{
				mv.invokeVirtual(new MethodInstance(method));
			}
		}
		else
		{
			Field field = ((FieldPropertyInfo) property).getBackingField();
			mv.getField(new FieldInstance(field));
		}
	}

	protected void invokeSetProperty(MethodGenerator mv, IPropertyInfo property)
	{
		if (property instanceof MethodPropertyInfo)
		{
			Method method = ((MethodPropertyInfo) property).getSetter();
			if (method.getDeclaringClass().isInterface())
			{
				mv.invokeInterface(new MethodInstance(method));
			}
			else
			{
				mv.invokeVirtual(new MethodInstance(method));
			}
		}
		else
		{
			Field field = ((FieldPropertyInfo) property).getBackingField();
			mv.putField(new FieldInstance(field));
		}
	}
}
