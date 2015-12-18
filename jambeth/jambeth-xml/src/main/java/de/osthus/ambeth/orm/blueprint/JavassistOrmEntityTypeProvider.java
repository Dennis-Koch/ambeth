package de.osthus.ambeth.orm.blueprint;

import java.util.Collection;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import de.osthus.ambeth.collections.WeakHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.IOrmEntityTypeProvider;

public class JavassistOrmEntityTypeProvider implements IOrmEntityTypeProvider, IStartingBean
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintProvider blueprintProvider;

	protected ClassPool pool;

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

		CtClass interf = pool.makeInterface(entityTypeName);
		try
		{
			if (entityTypeBlueprint.getInherits() != null)
			{
				for (String anInterface : entityTypeBlueprint.getInherits())
				{
					interf.addInterface(pool.get(anInterface));
				}
			}

			ConstPool constPool = interf.getClassFile().getConstPool();
			if (entityTypeBlueprint.getAnnotations() != null)
			{
				AnnotationsAttribute interfAnnotationAttributeInfo = createAnnotationAttribute(entityTypeBlueprint.getAnnotations(), constPool);
				interf.getClassFile().addAttribute(interfAnnotationAttributeInfo);
			}

			if (entityTypeBlueprint.getProperties() != null)
			{
				for (IEntityPropertyBlueprint prop : entityTypeBlueprint.getProperties())
				{
					CtClass resultType = pool.get(prop.getType());
					CtMethod ctGetMethod = new CtMethod(resultType, "get" + prop.getName(), null, interf);
					interf.addMethod(ctGetMethod);
					AnnotationsAttribute annotationAttributeInfo = createAnnotationAttribute(prop.getAnnotations(), constPool);
					ctGetMethod.getMethodInfo().addAttribute(annotationAttributeInfo);
					if (!prop.isReadonly())
					{
						CtClass[] parameters = new CtClass[] { resultType };
						CtMethod ctSetMethod = new CtMethod(CtClass.voidType, "set" + prop.getName(), parameters, interf);
						interf.addMethod(ctSetMethod);

					}
				}
			}

			Class<?> entityType;

			entityType = interf.toClass();

			alreadLoadedClasses.put(entityTypeName, entityType);
			return entityType;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected AnnotationsAttribute createAnnotationAttribute(Collection<IEntityAnnotationBlueprint> annotations, ConstPool constPool)
	{
		AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		for (IEntityAnnotationBlueprint anno : annotations)
		{
			Annotation annot = new Annotation(anno.getType(), constPool);
			for (IEntityAnnotationPropertyBlueprint annonProp : anno.getProperties())
			{
				// TODO: Determine member type and set
				// annot.addMemberValue(annonProp.getName(), new StringMemberValue(annonProp.getValue(), constPool));
			}
			attr.addAnnotation(annot);
		}
		return attr;
	}

	@Override
	public void afterStarted() throws Throwable
	{
		pool = ClassPool.getDefault();
	}

}
