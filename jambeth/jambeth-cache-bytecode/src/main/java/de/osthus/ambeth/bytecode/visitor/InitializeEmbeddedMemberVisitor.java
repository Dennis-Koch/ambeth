package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.template.EmbeddedMemberTemplate;
import de.osthus.ambeth.typeinfo.FieldPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;

public class InitializeEmbeddedMemberVisitor extends ClassGenerator
{
	public static final Class<?> templateType = EmbeddedMemberTemplate.class;

	public static final String templatePropertyName = templateType.getSimpleName();

	protected static final MethodInstance template_m_createEmbeddedObject = new MethodInstance(null, templateType, Object.class, "createEmbeddedObject",
			Class.class, Class.class, Object.class, String.class);

	public static PropertyInstance getEmbeddedMemberTemplatePI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
		if (pi != null)
		{
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public static boolean isEmbeddedMember(IEntityMetaData metaData, String name)
	{
		String[] nameSplit = name.split(Pattern.quote("."));
		for (Member member : metaData.getPrimitiveMembers())
		{
			if (!(member instanceof IEmbeddedMember))
			{
				continue;
			}
			if (((IEmbeddedMember) member).getMemberPath()[0].getName().equals(nameSplit[0]))
			{
				return true;
			}
		}
		for (RelationMember member : metaData.getRelationMembers())
		{
			if (!(member instanceof IEmbeddedMember))
			{
				continue;
			}
			if (((IEmbeddedMember) member).getMemberPath()[0].getName().equals(nameSplit[0]))
			{
				return true;
			}
		}
		return false;
	}

	protected IPropertyInfoProvider propertyInfoProvider;

	protected IEntityMetaData metaData;

	protected String memberPath;

	protected String[] memberPathSplit;

	public InitializeEmbeddedMemberVisitor(ClassVisitor cv, IEntityMetaData metaData, String memberPath, IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.metaData = metaData;
		this.memberPath = memberPath;
		this.memberPathSplit = memberPath != null ? memberPath.split(Pattern.quote(".")) : null;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		PropertyInstance p_embeddedMemberTemplate = getEmbeddedMemberTemplatePI(this);

		IdentityHashSet<Member> alreadyHandledFirstMembers = new IdentityHashSet<Member>();

		for (Member member : metaData.getPrimitiveMembers())
		{
			handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
		}
		for (RelationMember member : metaData.getRelationMembers())
		{
			handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
		}
		super.visitEnd();
	}

	protected void handleMember(PropertyInstance p_embeddedMemberTemplate, Member member, Set<Member> alreadyHandledFirstMembers)
	{
		if (!(member instanceof IEmbeddedMember))
		{
			return;
		}
		Member[] memberPath = ((IEmbeddedMember) member).getMemberPath();

		Member firstMember;
		if (memberPathSplit != null)
		{
			if (memberPath.length > memberPathSplit.length)
			{
				firstMember = memberPath[memberPathSplit.length];
			}
			else
			{
				// nothing to do in this case
				return;
			}
		}
		else
		{
			firstMember = memberPath[0];
		}
		if (!alreadyHandledFirstMembers.add(firstMember))
		{
			return;
		}
		implementGetter(p_embeddedMemberTemplate, firstMember, this.memberPath != null ? this.memberPath : firstMember.getName());
	}

	// protected void implementGetter(IProperty)
	// {
	// MethodGenerator mv = visitMethod(template_m_getValue);
	//
	// for (int a = 0, size = propertyPath.length; a < size; a++)
	// {
	// IPropertyInfo property = propertyPath[a];
	// if (property instanceof MethodPropertyInfo && ((MethodPropertyInfo) property).getGetter() == null)
	// {
	// throw new IllegalStateException("Property not readable: " + property.getDeclaringType().getName() + "." + property.getName());
	// }
	// }
	// Label l_pathHasNull = null;
	// mv.loadArg(0);
	// mv.checkCast(propertyPath[0].getDeclaringType());
	// for (int a = 0, size = propertyPath.length - 1; a < size; a++)
	// {
	// if (l_pathHasNull == null)
	// {
	// l_pathHasNull = mv.newLabel();
	// }
	// invokeGetProperty(mv, propertyPath[a]);
	// mv.dup();
	// mv.ifNull(l_pathHasNull);
	// }
	// IPropertyInfo lastProperty = propertyPath[propertyPath.length - 1];
	// Type lastPropertyType = Type.getType(lastProperty.getPropertyType());
	// invokeGetProperty(mv, lastProperty);
	// mv.valueOf(lastPropertyType);
	// mv.returnValue();
	//
	// if (l_pathHasNull != null)
	// {
	// mv.mark(l_pathHasNull);
	// if (lastProperty.getPropertyType().isPrimitive())
	// {
	// mv.pop(); // remove the current null value
	// mv.pushNullOrZero(lastPropertyType);
	// mv.valueOf(lastPropertyType);
	// }
	// mv.returnValue();
	// }
	// mv.endMethod();
	// }

	protected void implementGetter(final PropertyInstance p_embeddedMemberTemplate, final Member firstMember, final String memberPath)
	{
		PropertyInstance property = PropertyInstance.findByTemplate(firstMember.getName(), firstMember.getRealType(), false);

		final PropertyInstance p_rootEntity = memberPathSplit == null ? null : EmbeddedTypeVisitor.getRootEntityProperty(this);

		MethodGenerator mv = visitMethod(property.getGetter());
		Label l_valueIsValid = mv.newLabel();

		mv.loadThis();
		mv.invokeSuperOfCurrentMethod();
		mv.dup(); // cache member value

		mv.ifNonNull(l_valueIsValid);

		mv.pop(); // remove remaining null value

		mv.callThisSetter(property, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				// Object p_embeddedMemberTemplate.createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String memberPath)
				mg.callThisGetter(p_embeddedMemberTemplate);

				mg.push(firstMember.getRealType()); // embeddedType

				if (p_rootEntity != null)
				{
					mg.callThisGetter(p_rootEntity);
					mg.checkCast(EntityMetaDataHolderVisitor.m_template_getEntityMetaData.getOwner());
					mg.invokeInterface(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
				}
				else
				{
					mg.callThisGetter(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
				}
				mg.invokeInterface(new MethodInstance(null, IEntityMetaData.class, Class.class, "getEnhancedType"));
				mg.loadThis(); // parentObject
				mg.push(memberPath);

				mg.invokeVirtual(template_m_createEmbeddedObject);
				mg.checkCast(firstMember.getRealType());
			}
		});
		mv.loadThis();
		mv.invokeSuperOfCurrentMethod();
		mv.mark(l_valueIsValid);
		mv.returnValue();
		mv.endMethod();
	}

	protected void invokeGetProperty(MethodGenerator mv, IPropertyInfo property)
	{
		if (property instanceof MethodPropertyInfo)
		{
			Method method = ((MethodPropertyInfo) property).getGetter();
			mv.invokeVirtual(new MethodInstance(method));
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
			mv.invokeVirtual(new MethodInstance(method));
		}
		else
		{
			Field field = ((FieldPropertyInfo) property).getBackingField();
			mv.putField(new FieldInstance(field));
		}
	}
}
