package com.koch.ambeth.cache.bytecode.visitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.IOverrideConstructorDelegate;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.mixin.EmbeddedMemberMixin;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.EmbeddedMember;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class InitializeEmbeddedMemberVisitor extends ClassGenerator
{
	public static final Class<?> templateType = EmbeddedMemberMixin.class;

	public static final String templatePropertyName = "__" + templateType.getSimpleName();

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
		String[] nameSplit = EmbeddedMember.split(name);
		for (Member member : metaData.getPrimitiveMembers())
		{
			if (!(member instanceof IEmbeddedMember))
			{
				continue;
			}
			if (((IEmbeddedMember) member).getMemberPathToken()[0].equals(nameSplit[0]))
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
			if (((IEmbeddedMember) member).getMemberPathToken()[0].equals(nameSplit[0]))
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
		this.memberPathSplit = memberPath != null ? EmbeddedMember.split(memberPath) : null;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		PropertyInstance p_embeddedMemberTemplate = getEmbeddedMemberTemplatePI(this);
		implementConstructor(p_embeddedMemberTemplate);

		super.visitEnd();
	}

	protected void implementConstructor(PropertyInstance p_embeddedMemberTemplate)
	{
		HashSet<Member> alreadyHandledFirstMembers = new HashSet<Member>();

		final ArrayList<Script> scripts = new ArrayList<Script>();

		{
			Script script = handleMember(p_embeddedMemberTemplate, metaData.getIdMember(), alreadyHandledFirstMembers);
			if (script != null)
			{
				scripts.add(script);
			}
		}
		for (Member member : metaData.getPrimitiveMembers())
		{
			Script script = handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
			if (script != null)
			{
				scripts.add(script);
			}
		}
		for (RelationMember member : metaData.getRelationMembers())
		{
			Script script = handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
			if (script != null)
			{
				scripts.add(script);
			}
		}
		if (scripts.size() == 0)
		{
			return;
		}
		overrideConstructors(new IOverrideConstructorDelegate()
		{
			@Override
			public void invoke(ClassGenerator cv, ConstructorInstance superConstructor)
			{
				MethodGenerator mv = cv.visitMethod(superConstructor);
				mv.loadThis();
				mv.loadArgs();
				mv.invokeSuperOfCurrentMethod();

				for (Script script : scripts)
				{
					script.execute(mv);
				}
				mv.returnValue();
				mv.endMethod();
			}
		});
	}

	protected Script handleMember(PropertyInstance p_embeddedMemberTemplate, Member member, Set<Member> alreadyHandledFirstMembers)
	{
		if (member instanceof CompositeIdMember)
		{
			PrimitiveMember[] members = ((CompositeIdMember) member).getMembers();
			Script aggregatedScript = null;
			for (int a = 0, size = members.length; a < size; a++)
			{
				final Script script = handleMember(p_embeddedMemberTemplate, members[a], alreadyHandledFirstMembers);
				if (script == null)
				{
					continue;
				}
				if (aggregatedScript == null)
				{
					aggregatedScript = script;
					continue;
				}
				final Script oldAggregatedScript = aggregatedScript;
				aggregatedScript = new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						oldAggregatedScript.execute(mg);
						script.execute(mg);
					}
				};
			}
			return aggregatedScript;
		}
		if (!(member instanceof IEmbeddedMember))
		{
			return null;
		}
		Member[] memberPath = ((IEmbeddedMember) member).getMemberPath();
		Member firstMember;
		if (memberPathSplit != null)
		{
			if (memberPath.length < memberPathSplit.length)
			{
				// nothing to do in this case. This member has nothing to do with our current scope
				return null;
			}
			for (int a = 0, size = memberPathSplit.length; a < size; a++)
			{
				if (!memberPathSplit[a].equals(memberPath[a].getName()))
				{
					// nothing to do in this case. This member has nothing to do with our current scope
					return null;
				}
			}
			if (memberPath.length > memberPathSplit.length)
			{
				firstMember = memberPath[memberPathSplit.length];
			}
			else
			{
				// nothing to do in this case. This is a leaf member
				return null;
			}
		}
		else
		{
			firstMember = memberPath[0];
		}
		if (!alreadyHandledFirstMembers.add(firstMember))
		{
			return null;
		}
		return createEmbeddedObjectInstance(p_embeddedMemberTemplate, firstMember, this.memberPath != null ? this.memberPath + "." + firstMember.getName()
				: firstMember.getName());
	}

	protected Script createEmbeddedObjectInstance(final PropertyInstance p_embeddedMemberTemplate, final Member firstMember, final String memberPath)
	{
		final PropertyInstance property = PropertyInstance.findByTemplate(firstMember.getName(), firstMember.getRealType(), false);
		final PropertyInstance p_rootEntity = memberPathSplit == null ? null : EmbeddedTypeVisitor.getRootEntityProperty(this);

		return new Script()
		{
			@Override
			public void execute(MethodGenerator mg2)
			{
				mg2.callThisSetter(property, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						// Object p_embeddedMemberTemplate.createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String
						// memberPath)
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
			}
		};
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
