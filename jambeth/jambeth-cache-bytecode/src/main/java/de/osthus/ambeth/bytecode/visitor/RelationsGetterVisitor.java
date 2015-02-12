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
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.cache.ValueHolderIEC.ValueHolderContainerEntry;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.mixin.ValueHolderContainerMixin;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;

public class RelationsGetterVisitor extends ClassGenerator
{
	public class ValueHolderContainerEntryValueResolver implements IValueResolveDelegate
	{
		private final ValueHolderIEC valueHolderContainerHelper;

		public ValueHolderContainerEntryValueResolver(ValueHolderIEC valueHolderContainerHelper)
		{
			this.valueHolderContainerHelper = valueHolderContainerHelper;
		}

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
	}

	public static final Class<?> templateType = ValueHolderContainerMixin.class;

	protected static final String templatePropertyName = "__" + templateType.getSimpleName();

	private static final Type objRefArrayType = Type.getType(IObjRef[].class);

	private static final Type stateType = Type.getType(ValueHolderState.class);

	private static final PropertyInstance p_template_targetCache = PropertyInstance.findByTemplate(IValueHolderContainer.class, "__TargetCache",
			ICacheIntern.class, false);

	private static final MethodInstance m_vhce_getState_Member = new MethodInstance(null, ValueHolderContainerEntry.class, ValueHolderState.class, "getState",
			Object.class, int.class);

	private static final MethodInstance m_vhce_setInitPending_Member = new MethodInstance(null, ValueHolderContainerEntry.class, void.class, "setInitPending",
			Object.class, int.class);

	private static final MethodInstance m_vhce_isInitialized_Member = new MethodInstance(null, ValueHolderContainerEntry.class, boolean.class, "isInitialized",
			Object.class, int.class);

	private static final MethodInstance m_vhce_getObjRefs_Member = new MethodInstance(null, ValueHolderContainerEntry.class, IObjRef[].class, "getObjRefs",
			Object.class, int.class);

	private static final MethodInstance m_vhce_setObjRefs_Member = new MethodInstance(null, ValueHolderContainerEntry.class, void.class, "setObjRefs",
			Object.class, int.class, IObjRef[].class);

	private static final MethodInstance m_vhce_getValueDirect_Member = new MethodInstance(null, ValueHolderContainerEntry.class, Object.class,
			"getValueDirect", Object.class, int.class);

	private static final MethodInstance m_vhce_setValueDirect_Member = new MethodInstance(null, ValueHolderContainerEntry.class, void.class, "setValueDirect",
			Object.class, int.class, Object.class);

	private static final MethodInstance m_vhce_setUninitialized_Member = new MethodInstance(null, ValueHolderContainerEntry.class, void.class,
			"setUninitialized", Object.class, int.class, IObjRef[].class);

	private static final MethodInstance m_template_getCache = new MethodInstance(null, IObjRefContainer.class, ICache.class, "get__Cache");

	private static final MethodInstance m_template_getState_Member = new MethodInstance(null, IObjRefContainer.class, ValueHolderState.class, "get__State",
			int.class);

	private static final MethodInstance m_template_setInitPending_Member = new MethodInstance(null, IValueHolderContainer.class, void.class,
			"set__InitPending", int.class);

	public static final MethodInstance m_template_isInitialized_Member = new MethodInstance(null, IObjRefContainer.class, boolean.class, "is__Initialized",
			int.class);

	private static final MethodInstance m_template_getObjRefs_Member = new MethodInstance(null, IObjRefContainer.class, IObjRef[].class, "get__ObjRefs",
			int.class);

	private static final MethodInstance m_template_setObjRefs_Member = new MethodInstance(null, IObjRefContainer.class, void.class, "set__ObjRefs", int.class,
			IObjRef[].class);

	public static final MethodInstance m_template_getValueDirect_Member = new MethodInstance(null, IValueHolderContainer.class, Object.class,
			"get__ValueDirect", int.class);

	private static final MethodInstance m_template_setValueDirect_Member = new MethodInstance(null, IValueHolderContainer.class, void.class,
			"set__ValueDirect", int.class, Object.class);

	private static final MethodInstance m_template_setUninitialized_Member = new MethodInstance(null, IValueHolderContainer.class, void.class,
			"set__Uninitialized", int.class, IObjRef[].class);

	private static final MethodInstance m_template_getSelf = new MethodInstance(null, templateType, IObjRelation.class, "getSelf", IObjRefContainer.class,
			int.class);

	private static final MethodInstance m_template_getValue = new MethodInstance(null, templateType, Object.class, "getValue", IObjRefContainer.class,
			RelationMember[].class, int.class, ICacheIntern.class, IObjRef[].class);

	public static PropertyInstance getValueHolderContainerTemplatePI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
		if (pi != null)
		{
			return pi;
		}
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

	private final IPropertyInfoProvider propertyInfoProvider;

	private final ValueHolderIEC valueHolderContainerHelper;

	public RelationsGetterVisitor(ClassVisitor cv, IEntityMetaData metaData, ValueHolderIEC valueHolderContainerHelper,
			IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.metaData = metaData;
		this.valueHolderContainerHelper = valueHolderContainerHelper;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		PropertyInstance p_valueHolderContainerTemplate = getValueHolderContainerTemplatePI(this);
		PropertyInstance p_relationMembers = implementAssignedReadonlyProperty("__RelationMembers", metaData.getRelationMembers());

		PropertyInstance p_targetCache = implementTargetCache(p_valueHolderContainerTemplate);

		if (!EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			PropertyInstance p_valueHolderContainerEntry = implementAssignedReadonlyProperty("ValueHolderContainerEntry",
					new ValueHolderContainerEntryValueResolver(valueHolderContainerHelper));
			implementGetState(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementSetInitPending(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementIsInitialized(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementGetObjRefs(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementSetObjRefs(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementGetValueDirect(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementSetValueDirect(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
			implementSetUninitialized(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
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
		RelationMember[] relationMembers = metaData.getRelationMembers();
		final ArrayList<FieldInstance[]> fieldsList = new ArrayList<FieldInstance[]>();

		for (int a = relationMembers.length; a-- > 0;)
		{
			RelationMember relationMember = relationMembers[a];
			relationMember = (RelationMember) getApplicableMember(relationMember);
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

	protected void implementSetInitPending(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_setInitPending_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_setInitPending_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementIsInitialized(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_isInitialized_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_isInitialized_Member);
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

	protected void implementGetValueDirect(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_getValueDirect_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_getValueDirect_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementSetValueDirect(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_setValueDirect_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_setValueDirect_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementSetObjRefs(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_setObjRefs_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_setObjRefs_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementSetUninitialized(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
	{
		{
			MethodGenerator mv = visitMethod(m_template_setUninitialized_Member);
			mv.callThisGetter(p_valueHolderContainerEntry);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeVirtual(m_vhce_setUninitialized_Member);
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected PropertyInstance implementTargetCache(PropertyInstance p_valueHolderContainerTemplate)
	{
		if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			final PropertyInstance p_rootEntity = EmbeddedTypeVisitor.getRootEntityProperty(this);
			PropertyInstance p_targetCache = implementProperty(p_template_targetCache, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					Label l_finish = mg.newLabel();
					mg.callThisGetter(p_rootEntity);
					mg.dup();
					mg.ifNull(l_finish);
					mg.checkCast(IObjRefContainer.class);
					mg.invokeInterface(p_template_targetCache.getGetter());
					mg.mark(l_finish);
					mg.returnValue();
				}
			}, null);

			{
				MethodGenerator mg = visitMethod(m_template_getCache);
				mg.callThisGetter(p_targetCache);
				mg.returnValue();
				mg.endMethod();
			}
			return p_targetCache;
		}
		implementSelfGetter(p_valueHolderContainerTemplate);

		final FieldInstance f_targetCache = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__targetCache", p_template_targetCache.getSignature(),
				p_template_targetCache.getPropertyType()));

		PropertyInstance p_targetCache = implementProperty(p_template_targetCache, new Script()
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
		{
			MethodGenerator mg = visitMethod(m_template_getCache);
			mg.callThisGetter(p_targetCache);
			mg.returnValue();
			mg.endMethod();
		}
		return p_targetCache;
	}

	protected void implementValueHolderCode(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_targetCache, PropertyInstance p_relationMembers)
	{
		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
		{
			RelationMember relationMember = relationMembers[relationIndex];
			relationMember = (RelationMember) getApplicableMember(relationMember);
			if (relationMember == null)
			{
				// member is handled in another type
				continue;
			}
			String propertyName = relationMember.getName();
			IPropertyInfo propertyInfo = propertyInfoProvider.getProperty(relationMember.getDeclaringType(), propertyName);
			PropertyInstance prop = PropertyInstance.findByTemplate(propertyInfo, true);
			MethodInstance m_get = prop != null ? prop.getGetter() : new MethodInstance(((MethodPropertyInfo) propertyInfo).getGetter());
			MethodInstance m_set = prop != null ? prop.getSetter() : new MethodInstance(((MethodPropertyInfo) propertyInfo).getSetter());

			FieldInstance f_objRefs = getObjRefsFieldByPropertyName(propertyName);
			FieldInstance f_objRefs_existing = getState().getAlreadyImplementedField(f_objRefs.getName());

			FieldInstance f_initialized = getInitializedFieldByPropertyName(propertyName);
			FieldInstance f_initialized_existing = getState().getAlreadyImplementedField(f_initialized.getName());

			if (f_objRefs_existing == null)
			{
				f_objRefs_existing = implementField(f_objRefs);
				implementGetter(new MethodInstance(null, Opcodes.ACC_PUBLIC, f_objRefs_existing.getType(), "get" + f_objRefs_existing.getName(), null),
						f_objRefs_existing);
				implementSetter(
						new MethodInstance(null, Opcodes.ACC_PUBLIC, Type.VOID_TYPE, "set" + f_objRefs_existing.getName(), null, f_objRefs_existing.getType()),
						f_objRefs_existing);
			}
			if (f_initialized_existing == null)
			{
				f_initialized_existing = implementField(f_initialized);
				implementGetter(new MethodInstance(null, Opcodes.ACC_PUBLIC, f_initialized_existing.getType(), "get" + f_initialized_existing.getName(), null),
						f_initialized_existing);
				implementSetter(new MethodInstance(null, Opcodes.ACC_PUBLIC, Type.VOID_TYPE, "set" + f_initialized_existing.getName(), null,
						f_initialized_existing.getType()), f_initialized_existing);
			}

			implementRelationGetter(propertyName, m_get, m_set, relationIndex, p_valueHolderContainerTemplate, p_targetCache, p_relationMembers,
					f_initialized_existing, f_objRefs_existing);
			implementRelationSetter(propertyName, m_set, f_initialized_existing, f_objRefs_existing);
		}
	}

	public static Member getApplicableMember(Member relationMember)
	{
		String propertyName = relationMember.getName();
		if (relationMember instanceof IEmbeddedMember)
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
				if (relationMember instanceof IEmbeddedMember)
				{
					relationMember = ((IEmbeddedMember) relationMember).getChildMember();
				}
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
		MethodInstance m_getSelf = new MethodInstance(owner, IValueHolderContainer.class, IObjRelation.class, "get__Self", int.class);
		{
			// public IObjRelation getSelf(int relationIndex)
			// {
			// return valueHolderContainerTemplate.getSelf(this, relationIndex);
			// }
			MethodGenerator mv = visitMethod(m_getSelf);
			mv.callThisGetter(p_valueHolderContainerTemplate);
			// this
			mv.loadThis();
			// relationIndex
			mv.loadArgs();
			mv.invokeVirtual(m_template_getSelf);
			mv.returnValue();
			mv.endMethod();
		}
		// MethodInstance m_getSelf = new MethodInstance(owner, IValueHolderContainer.class, IObjRelation.class, "get__Self", String.class);
		// {
		// // public IObjRelation getSelf(String memberName)
		// // {
		// // return RelationsGetterVisitor.valueHolderContainer_getSelf(this, this.$beanContext, memberName);
		// // }
		// MethodGenerator mv = visitMethod(m_getSelf);
		// mv.callThisGetter(p_valueHolderContainerTemplate);
		// // this
		// mv.loadThis();
		// // memberName
		// mv.loadArgs();
		// mv.invokeVirtual(m_template_getSelf);
		// mv.returnValue();
		// mv.endMethod();
		// }
		// {
		// // public IObjRelation getSelf(IRelationInfoItem member)
		// // {
		// // return getSelf(member.getName());
		// // }
		// MethodInstance method = new MethodInstance(owner, IValueHolderContainer.class, IObjRelation.class, "get__Self", IRelationInfoItem.class);
		// MethodGenerator mv = visitMethod(method);
		// mv.loadThis();
		// mv.loadArg(0);
		// mv.invokeInterface(new MethodInstance(null, INamed.class, String.class, "getName"));
		// mv.invokeVirtual(m_getSelf);
		// mv.returnValue();
		// mv.endMethod();
		// }
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
					Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, Type.VOID_TYPE, propertyName + "$doInitialize", null);
			{
				MethodGenerator mg = visitMethod(m_getMethod_scoped);

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
			PropertyInstance p_getNoInit = PropertyInstance.findByTemplate(propertyName + ValueHolderIEC.getNoInitSuffix(), m_getNoInit.getReturnType(), false);
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