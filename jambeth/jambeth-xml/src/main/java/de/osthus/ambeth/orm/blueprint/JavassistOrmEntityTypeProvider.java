package de.osthus.ambeth.orm.blueprint;

import java.util.Collection;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import de.osthus.ambeth.collections.WeakHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.IOrmEntityTypeProvider;
import de.osthus.ambeth.util.IConversionHelper;

public class JavassistOrmEntityTypeProvider implements IOrmEntityTypeProvider, IStartingBean
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintProvider blueprintProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	protected ClassPool pool;

	protected CtClass stringClass;

	protected WeakHashMap<String, Class<?>> alreadLoadedClasses = new WeakHashMap<String, Class<?>>();

	@Override
	public Class<?> resolveEntityType(String entityTypeName)
	{
		if (blueprintProvider == null)
		{
			throw new IllegalStateException("No " + IBlueprintProvider.class.getName() + " injected. This is an illegal state");
		}

		if (alreadLoadedClasses.containsKey(entityTypeName))
		{
			return alreadLoadedClasses.get(entityTypeName);
		}
		IEntityTypeBlueprint entityTypeBlueprint = blueprintProvider.resolveEntityTypeBlueprint(entityTypeName);

		CtClass newClass;
		if (entityTypeBlueprint.getIsClass())
		{
			newClass = pool.makeClass(entityTypeName);
		}
		else
		{
			newClass = pool.makeInterface(entityTypeName);
		}
		try
		{
			Class<?> defaultInterface = blueprintProvider.getDefaultInterface();
			if (entityTypeBlueprint.getInterfaces() != null)
			{
				for (String aClass : entityTypeBlueprint.getInterfaces())
				{
					newClass.addInterface(pool.get(aClass));
				}
			}

			if (entityTypeBlueprint.getSuperclass() != null)
			{
				if (entityTypeBlueprint.getIsClass())
				{
					newClass.setSuperclass(pool.get(entityTypeBlueprint.getSuperclass()));
				}
				else
				{
					throw new IllegalArgumentException(entityTypeBlueprint.getName() + " is an interface but has a superclass.");
				}
			}

			ConstPool constPool = newClass.getClassFile().getConstPool();
			if (entityTypeBlueprint.getAnnotations() != null)
			{
				AnnotationsAttribute interfAnnotationAttributeInfo = createAnnotationAttribute(entityTypeBlueprint.getAnnotations(), constPool);
				newClass.getClassFile().addAttribute(interfAnnotationAttributeInfo);
			}

			if (entityTypeBlueprint.getProperties() != null)
			{
				for (IEntityPropertyBlueprint prop : entityTypeBlueprint.getProperties())
				{
					if (entityTypeBlueprint.getIsClass())
					{
						CtField ctField = new CtField(pool.get(prop.getType()), prop.getName(), newClass);
						newClass.addField(ctField);
						newClass.addMethod(CtNewMethod.getter("get" + prop.getName(), ctField));
						newClass.addMethod(CtNewMethod.setter("set" + prop.getName(), ctField));
					}
					else
					{
						CtClass resultType = pool.get(prop.getType());
						CtMethod ctGetMethod = new CtMethod(resultType, "get" + prop.getName(), null, newClass);
						newClass.addMethod(ctGetMethod);
						AnnotationsAttribute annotationAttributeInfo = createAnnotationAttribute(prop.getAnnotations(), constPool);
						ctGetMethod.getMethodInfo().addAttribute(annotationAttributeInfo);
						if (!prop.isReadonly())
						{
							CtClass[] parameters = new CtClass[] { resultType };
							CtMethod ctSetMethod = new CtMethod(CtClass.voidType, "set" + prop.getName(), parameters, newClass);
							newClass.addMethod(ctSetMethod);

						}
					}
				}
			}

			Class<?> entityType;

			entityType = newClass.toClass();

			alreadLoadedClasses.put(entityTypeName, entityType);
			return entityType;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected AnnotationsAttribute createAnnotationAttribute(Collection<? extends IEntityAnnotationBlueprint> annotations, ConstPool constPool)
			throws Exception
	{
		AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		for (IEntityAnnotationBlueprint anno : annotations)
		{
			Annotation annot = new Annotation(anno.getType(), constPool);
			CtClass annotClass = pool.get(anno.getType());

			for (IEntityAnnotationPropertyBlueprint annonProp : anno.getProperties())
			{
				CtMethod annonPropMethod = annotClass.getDeclaredMethod(annonProp.getName());

				if (annonPropMethod.getReturnType().equals(CtClass.booleanType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new BooleanMemberValue(conversionHelper.convertValueToType(Boolean.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.byteType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new ByteMemberValue(conversionHelper.convertValueToType(Byte.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.charType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new CharMemberValue(conversionHelper.convertValueToType(Character.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.doubleType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new DoubleMemberValue(conversionHelper.convertValueToType(Double.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.floatType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new FloatMemberValue(conversionHelper.convertValueToType(Float.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.intType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new IntegerMemberValue(conversionHelper.convertValueToType(Integer.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.longType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new LongMemberValue(conversionHelper.convertValueToType(Long.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.shortType))
				{
					annot.addMemberValue(annonProp.getName(), //
							new ShortMemberValue(conversionHelper.convertValueToType(Short.class, annonProp.getValue()), constPool));
				}
				else if (annonPropMethod.getReturnType().equals(stringClass))
				{
					annot.addMemberValue(annonProp.getName(), //
							new StringMemberValue(conversionHelper.convertValueToType(String.class, annonProp.getValue()), constPool));
				}
			}
			attr.addAnnotation(annot);
		}
		return attr;
	}

	@Override
	public void afterStarted() throws Throwable
	{
		pool = ClassPool.getDefault();
		stringClass = pool.get(String.class.getName());
	}

}
