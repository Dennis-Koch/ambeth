package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IOverrideConstructorDelegate;
import de.osthus.ambeth.bytecode.IValueResolveDelegate;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.cache.ValueHolderIEC.ValueHolderContainerEntry;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.typeinfo.EmbeddedRelationInfoItem;
import de.osthus.ambeth.typeinfo.IEmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.INamed;

public class RelationsGetterVisitor extends ClassGenerator
{
	public static final Class<?> templateType = ValueHolderContainerTemplate.class;

	protected static final String templatePropertyName = templateType.getSimpleName();

	private static final Type objRefArrayType = Type.getType(IObjRef[].class);

	private static final Type stateType = Type.getType(ValueHolderState.class);

	private static final PropertyInstance p_template_targetCache = PropertyInstance.findByTemplate(IValueHolderContainer.class, "__TargetCache", false);

	private static final MethodInstance m_vhce_getState_Member = new MethodInstance(null, ValueHolderContainerEntry.class, "getState", Object.class,
			IRelationInfoItem.class);

	private static final MethodInstance m_vhce_getObjRefs_Member = new MethodInstance(null, ValueHolderContainerEntry.class, "getObjRefs", Object.class,
			IRelationInfoItem.class);

	private static final MethodInstance m_template_getState_Member = new MethodInstance(null, IValueHolderContainer.class, "get__State",
			IRelationInfoItem.class);

	private static final MethodInstance m_template_getObjRefs_Member = new MethodInstance(null, IValueHolderContainer.class, "get__ObjRefs",
			IRelationInfoItem.class);

	private static final MethodInstance m_template_getSelf = new MethodInstance(null, templateType, "getSelf", Object.class, String.class);

	private static final MethodInstance m_template_getValue = new MethodInstance(null, templateType, "getValue", Object.class, IRelationInfoItem[].class,
			int.class, ICacheIntern.class, IObjRef[].class);

	public static PropertyInstance getValueHolderContainerTemplatePI(ClassGenerator cv)
	{
		PropertyInstance pi = getState().getProperty(templatePropertyName);
		if (pi != null)
		{
			return pi;
		}
		Object bean = getState().getBeanContext().getService(templateType);
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public static final FieldInstance getObjRefsField(String propertyName, boolean expectExistance)
	{
		String fieldName = ValueHolderIEC.getObjRefsFieldName(propertyName);
		FieldInstance field = BytecodeBehaviorState.getState().getAlreadyImplementedField(fieldName);
		if (field == null && expectExistance)
		{
			throw new IllegalStateException("Field not defined in type hierarchy: " + BytecodeBehaviorState.getState().getNewType().getClassName() + "."
					+ fieldName);
		}
		return field;
	}

	public static final FieldInstance getInitializedField(String propertyName, boolean expectExistance)
	{
		String fieldName = ValueHolderIEC.getInitializedFieldName(propertyName);
		FieldInstance field = BytecodeBehaviorState.getState().getAlreadyImplementedField(fieldName);
		if (field == null && expectExistance)
		{
			throw new IllegalStateException("Field not defined in type hierarchy: " + BytecodeBehaviorState.getState().getNewType().getClassName() + "."
					+ fieldName);
		}
		return field;
	}

	private final IEntityMetaData metaData;

	private final ValueHolderIEC valueHolderContainerHelper;

	public RelationsGetterVisitor(ClassVisitor cv, IEntityMetaData metaData, ValueHolderIEC valueHolderContainerHelper)
	{
		super(cv);
		this.metaData = metaData;
		this.valueHolderContainerHelper = valueHolderContainerHelper;
	}

	@Override
	public void visitEnd()
	{
		PropertyInstance p_valueHolderContainerTemplate = getValueHolderContainerTemplatePI(this);
		PropertyInstance p_relationMembers = implementAssignedReadonlyProperty("sf_$relationMembers", metaData.getRelationMembers());

		PropertyInstance p_targetCache = implementTargetCache(p_valueHolderContainerTemplate);

		if (!EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			PropertyInstance p_valueHolderContainerEntry = implementAssignedReadonlyProperty("ValueHolderContainerEntry", new IValueResolveDelegate()
			{
				@Override
				public Class<?> getValueType()
				{
					return ValueHolderContainerEntry.class;
				}

				@Override
				public Object invoke(String fieldName, Class<?> enhancedType)
				{
					return valueHolderContainerHelper.getVhcEntry(enhancedType);
				}
			});
			implementGetState(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementGetObjRefs(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
		}

		implementValueHolderCode(p_valueHolderContainerTemplate, p_targetCache, p_relationMembers);

		implementConstructors();
		super.visitEnd();
	}

	protected void implementConstructors()
	{
		if (metaData.getRelationMembers().length == 0)
		{
			return;
		}
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		final ArrayList<FieldInstance[]> fieldsList = new ArrayList<FieldInstance[]>();

		for (int a = relationMembers.length; a-- > 0;)
		{
			IRelationInfoItem relationMember = relationMembers[a];
			relationMember = getApplicableMember(relationMember);
			if (relationMember == null)
			{
				// member is handled in another type
				continue;
			}
			String propertyName = relationMember.getName();
			String fieldName = ValueHolderIEC.getObjRefsFieldName(propertyName);
			FieldInstance field = getState().getAlreadyImplementedField(fieldName);

			String fieldName2 = ValueHolderIEC.getInitializedFieldName(propertyName);
			FieldInstance field2 = getState().getAlreadyImplementedField(fieldName2);

			fieldsList.add(new FieldInstance[] { field, field2 });
		}
		if (fieldsList.size() == 0)
		{
			return;
		}
		final PropertyInstance p_emptyRelations = implementAssignedReadonlyProperty("EmptyRelations", ObjRef.EMPTY_ARRAY);

		overrideConstructors(new IOverrideConstructorDelegate()
		{
			@Override
			public void invoke(ClassGenerator cv, ConstructorInstance superConstructor)
			{
				MethodGenerator mv = cv.visitMethod(superConstructor);
				mv.loadThis();
				mv.loadArgs();
				mv.invokeSuperOfCurrentMethod();

				final int loc_emptyRelations = mv.newLocal(IObjRef[].class);
				final int loc_lazyState = mv.newLocal(ValueHolderState.class);
				mv.callThisGetter(p_emptyRelations);
				mv.storeLocal(loc_emptyRelations);
				mv.pushEnum(ValueHolderState.LAZY);
				mv.storeLocal(loc_lazyState);
				for (FieldInstance[] fields : fieldsList)
				{
					mv.putThisField(fields[0], new Script()
					{
						@Override
						public void execute(MethodGenerator mv2)
						{
							mv2.loadLocal(loc_emptyRelations);
						}
					});
					mv.putThisField(fields[1], new Script()
					{
						@Override
						public void execute(MethodGenerator mv2)
						{
							mv2.loadLocal(loc_lazyState);
						}
					});
				}
				mv.returnValue();
				mv.endMethod();
			}
		});
	}

	protected FieldInstance getObjRefsFieldByPropertyName(String propertyName)
	{
		String fieldName = ValueHolderIEC.getObjRefsFieldName(propertyName);

		FieldInstance field = getState().getAlreadyImplementedField(fieldName);
		if (field == null)
		{
			field = new FieldInstance(Opcodes.ACC_PUBLIC, fieldName, null, objRefArrayType);
		}
		return field;
	}

	protected FieldInstance getInitializedFieldByPropertyName(String propertyName)
	{
		String fieldName = ValueHolderIEC.getInitializedFieldName(propertyName);

		FieldInstance field = getState().getAlreadyImplementedField(fieldName);
		if (field == null)
		{
			field = new FieldInstance(Opcodes.ACC_PUBLIC, fieldName, null, stateType);
		}
		return field;
	}

	protected void implementGetState(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_getState_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_getState_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementGetObjRefs(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_getObjRefs_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_getObjRefs_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected PropertyInstance implementTargetCache(PropertyInstance p_valueHolderContainerTemplate)
	{
		if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			final PropertyInstance p_rootEntity = EmbeddedTypeVisitor.getRootEntityProperty(this);
			return implementProperty(p_template_targetCache, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					Label l_finish = mg.newLabel();
					mg.callThisGetter(p_rootEntity);
					mg.dup();
					mg.ifNull(l_finish);
					mg.checkCast(IValueHolderContainer.class);
					mg.invokeInterface(p_template_targetCache.getGetter());
					mg.mark(l_finish);
					mg.returnValue();
				}
			}, null);
		}
		implementSelfGetter(p_valueHolderContainerTemplate);

		final FieldInstance f_targetCache = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$targetCache", p_template_targetCache.getSignature(),
				p_template_targetCache.getPropertyType()));

		return implementProperty(p_template_targetCache, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.getThisField(f_targetCache);
				mg.returnValue();
			}
		}, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.putThisField(f_targetCache, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadArg(0);
					}
				});
				mg.returnValue();
			}
		});
	}

	protected void implementValueHolderCode(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_targetCache, PropertyInstance p_relationMembers)
	{
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
		{
			IRelationInfoItem relationMember = relationMembers[relationIndex];
			relationMember = getApplicableMember(relationMember);
			if (relationMember == null)
			{
				// member is handled in another type
				continue;
			}
			String propertyName = relationMember.getName();
			MethodInstance m_get = new MethodInstance(((MethodPropertyInfo) ((PropertyInfoItem) relationMember).getProperty()).getGetter());
			MethodInstance m_set = new MethodInstance(((MethodPropertyInfo) ((PropertyInfoItem) relationMember).getProperty()).getSetter());

			FieldInstance f_objRefs = getObjRefsFieldByPropertyName(propertyName);
			FieldInstance f_objRefs_existing = getState().getAlreadyImplementedField(f_objRefs.getName());

			FieldInstance f_initialized = getInitializedFieldByPropertyName(propertyName);
			FieldInstance f_initialized_existing = getState().getAlreadyImplementedField(f_initialized.getName());

			if (f_objRefs_existing == null)
			{
				f_objRefs_existing = implementField(f_objRefs);
			}
			if (f_initialized_existing == null)
			{
				f_initialized_existing = implementField(f_initialized);
			}

			implementRelationGetter(propertyName, m_get, m_set, relationIndex, p_valueHolderContainerTemplate, p_targetCache, p_relationMembers,
					f_initialized_existing, f_objRefs_existing);
			implementRelationSetter(propertyName, m_set, f_initialized_existing, f_objRefs_existing);
		}
	}

	protected IRelationInfoItem getApplicableMember(IRelationInfoItem relationMember)
	{
		String propertyName = relationMember.getName();
		if (relationMember instanceof IEmbeddedTypeInfoItem)
		{
			String memberPath = EmbeddedEnhancementHint.getMemberPath(getState().getContext());
			if (memberPath != null)
			{
				if (!propertyName.startsWith(memberPath + "."))
				{
					// This relation has to be handled by another embedded type
					return null;
				}
				propertyName = propertyName.substring(memberPath.length() + 1);
				if (propertyName.contains("."))
				{
					// This relation has to be handled by another child embedded type of this embedded type
					return null;
				}
				relationMember = ((EmbeddedRelationInfoItem) relationMember).getChildMember();
			}
			else if (propertyName.contains("."))
			{
				// This is an embedded member which will be implemented in the enhanced embedded type
				return null;
			}
		}
		else if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			// entities are already enhanced
			return null;
		}
		return relationMember;
	}

	protected void implementSelfGetter(PropertyInstance p_valueHolderContainerTemplate)
	{
		Type owner = BytecodeBehaviorState.getState().getNewType();
		MethodInstance m_getSelf = new MethodInstance(owner, IValueHolderContainer.class, "get__Self", String.class);
		{
			// public IObjRelation getSelf(String memberName)
			// {
			// return RelationsGetterVisitor.valueHolderContainer_getSelf(this, this.$beanContext, memberName);
			// }
			MethodGenerator mv = visitMethod(m_getSelf);
			mv.callThisGetter(p_valueHolderContainerTemplate);
			// this
			mv.loadThis();
			// memberName
			mv.loadArgs();
			mv.invokeVirtual(m_template_getSelf);
			mv.returnValue();
			mv.endMethod();
		}
		{
			// public IObjRelation getSelf(IRelationInfoItem member)
			// {
			// return getSelf(member.getName());
			// }
			MethodInstance method = new MethodInstance(owner, IValueHolderContainer.class, "get__Self", IRelationInfoItem.class);
			MethodGenerator mv = visitMethod(method);
			mv.loadThis();
			mv.loadArg(0);
			mv.invokeInterface(new MethodInstance(null, INamed.class, "getName"));
			mv.invokeVirtual(m_getSelf);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementRelationGetter(String propertyName, MethodInstance m_getMethod_template, final MethodInstance m_setMethod, final int relationIndex,
			final PropertyInstance p_valueHolderContainerTemplate, final PropertyInstance p_targetCache, final PropertyInstance p_relationMembers,
			FieldInstance f_initialized, final FieldInstance f_objRefs)
	{
		// public String getPropertyName()
		// {
		// if (!PropertyName$initialized)
		// {
		// setPropertyName(RelationsGetterVisitor.valueHolderContainer_getValue(this, $relationMembers, get__IndexOfPropertyName(), $targetCache, $beanContext,
		// propertyName$objRefs));
		// }
		// return super.getPropertyName();
		// }

		final Script script_getVHC;
		if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			final PropertyInstance p_rootEntity = EmbeddedTypeVisitor.getRootEntityProperty(this);
			script_getVHC = new Script()
			{
				@Override
				public void execute(MethodGenerator mv)
				{
					mv.callThisGetter(p_rootEntity);
				}
			};
		}
		else
		{
			script_getVHC = new Script()
			{
				@Override
				public void execute(MethodGenerator mv)
				{
					mv.loadThis();
				}
			};
		}

		MethodInstance m_getMethod;
		{
			PropertyInstance p_cacheModification = SetCacheModificationMethodCreator.getCacheModificationPI(this);
			final MethodInstance m_getMethod_scoped = new MethodInstance(BytecodeBehaviorState.getState().getNewType(),
					Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, m_getMethod_template.getName() + "$getValue", null, Type.VOID_TYPE);
			{
				MethodGenerator mg = super.visitMethod(m_getMethod_scoped);

				// property => for this.setPropertyName(...)
				mg.loadThis();
				// call template.getValue(..)
				mg.callThisGetter(p_valueHolderContainerTemplate);
				// getVhc()
				script_getVHC.execute(mg);
				// $relationMembers
				mg.callThisGetter(p_relationMembers);
				// get__IndexOfPropertyName()
				mg.push(relationIndex);
				// $targetCache
				mg.callThisGetter(p_targetCache);
				// propertyName$objRefs
				mg.getThisField(f_objRefs);
				mg.invokeVirtual(m_template_getValue);
				mg.checkCast(m_setMethod.getParameters()[0]);
				mg.invokeVirtual(m_setMethod);
				mg.returnValue();
				mg.endMethod();
			}
			MethodGenerator mg = super.visitMethod(m_getMethod_template);
			m_getMethod = mg.getMethod();
			Label l_initialized = mg.newLabel();
			mg.getThisField(f_initialized);
			mg.pushEnum(ValueHolderState.INIT);
			mg.ifCmp(f_initialized.getType(), GeneratorAdapter.EQ, l_initialized);

			SetCacheModificationMethodCreator.cacheModificationInternalUpdate(p_cacheModification, mg, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadThis();
					mg.invokeOnExactOwner(m_getMethod_scoped);
				}
			});

			mg.mark(l_initialized);
			mg.loadThis();
			mg.invokeSuperOfCurrentMethod();
			mg.returnValue();
			mg.endMethod();
		}

		// public String getPropertyName$NoInit()
		// {
		// return super.getPropertyName();
		// }
		{
			MethodInstance m_getNoInit = m_getMethod_template.deriveName(ValueHolderIEC.getGetterNameOfRelationPropertyWithNoInit(propertyName));
			MethodGenerator mg = super.visitMethod(m_getNoInit);
			PropertyInstance p_getNoInit = PropertyInstance.findByTemplate(propertyName + ValueHolderIEC.getNoInitSuffix(), false);
			p_getNoInit.addAnnotation(c_fireThisOPC, propertyName);
			p_getNoInit.addAnnotation(c_fireTargetOPC, propertyName);
			mg.loadThis();
			mg.invokeSuper(m_getMethod);
			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementRelationSetter(String propertyName, MethodInstance m_set_template, FieldInstance f_initialized, FieldInstance f_objRefs)
	{
		// public void setPropertyName(String propertyName)
		// {
		// PropertyName$initialized = true;
		// PropertyName$objRefs = null;
		// super.setPropertyName(propertyName);
		// }
		MethodInstance m_set;
		{
			MethodGenerator mg = super.visitMethod(m_set_template);
			m_set = mg.getMethod();
			mg.putThisField(f_initialized, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.pushEnum(ValueHolderState.INIT);
				}
			});
			mg.putThisField(f_objRefs, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.pushNull();
				}
			});
			mg.loadThis();
			mg.loadArgs();
			mg.invokeSuperOfCurrentMethod();
			mg.returnVoidOrThis();
			mg.endMethod();
		}

		// public void setPropertyName$NoInit(String propertyName)
		// {
		// super.setPropertyName(propertyName);
		// }
		{
			String noInitSetMethodName = ValueHolderIEC.getSetterNameOfRelationPropertyWithNoInit(propertyName);
			MethodGenerator mv = super.visitMethod(m_set.getAccess(), noInitSetMethodName, m_set.getDescriptor(), m_set.getSignature(), null);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeSuper(m_set);
			mv.returnVoidOrThis();
			mv.endMethod();
		}
	}
}