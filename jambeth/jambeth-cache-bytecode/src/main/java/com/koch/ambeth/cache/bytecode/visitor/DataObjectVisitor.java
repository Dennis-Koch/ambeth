package com.koch.ambeth.cache.bytecode.visitor;

import java.util.Collection;
import java.util.Iterator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.cache.mixin.DataObjectMixin;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.annotation.IgnoreToBeUpdated;
import com.koch.ambeth.util.annotation.ParentChild;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class DataObjectVisitor extends ClassGenerator
{
	public static final Class<?> templateType = DataObjectMixin.class;

	protected static final String templatePropertyName = "__" + templateType.getSimpleName();

	public static final MethodInstance m_toBeUpdatedChanged = new MethodInstance(null, templateType, void.class, "toBeUpdatedChanged", IDataObject.class,
			boolean.class, boolean.class);

	public static final PropertyInstance p_hasPendingChanges = PropertyInstance.findByTemplate(IDataObject.class, "HasPendingChanges", boolean.class, false);

	public static final PropertyInstance template_p_toBeCreated = PropertyInstance.findByTemplate(IDataObject.class, "ToBeCreated", boolean.class, false);

	public static final PropertyInstance template_p_toBeUpdated = PropertyInstance.findByTemplate(IDataObject.class, "ToBeUpdated", boolean.class, false);

	public static final PropertyInstance template_p_toBeDeleted = PropertyInstance.findByTemplate(IDataObject.class, "ToBeDeleted", boolean.class, false);

	public static final Class<IgnoreToBeUpdated> c_ignoreToBeUpdated = IgnoreToBeUpdated.class;

	public static PropertyInstance getDataObjectTemplatePI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance p_dataObjectTemplate = getState().getProperty(templatePropertyName, bean.getClass());
		if (p_dataObjectTemplate != null)
		{
			return p_dataObjectTemplate;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	protected final IEntityMetaData metaData;

	protected final IPropertyInfoProvider propertyInfoProvider;

	public DataObjectVisitor(ClassVisitor cv, IEntityMetaData metaData, IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.metaData = metaData;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		PropertyInstance p_toBeCreated = implementToBeCreated(template_p_toBeCreated);

		PropertyInstance p_toBeUpdated = implementToBeUpdated();

		// ToBeDeleted
		final FieldInstance f_toBeDeleted = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$toBeDeleted", null,
				template_p_toBeDeleted.getPropertyType()));

		PropertyInstance p_toBeDeleted = implementProperty(template_p_toBeDeleted, new Script()

		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.getThisField(f_toBeDeleted);
				mg.returnValue();
			}
		}, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.putThisField(f_toBeDeleted, new Script()
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
		p_toBeDeleted.addAnnotation(c_ignoreToBeUpdated);

		implementHasPendingChanges(p_hasPendingChanges, p_toBeUpdated, p_toBeCreated, p_toBeDeleted);

		super.visitEnd();
	}

	protected PropertyInstance implementToBeUpdated()
	{
		final PropertyInstance p_dataObjectTemplate = getDataObjectTemplatePI(this);

		final FieldInstance f_toBeUpdated = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$toBeUpdated", null,
				template_p_toBeUpdated.getPropertyType()));

		boolean atLeastOneToManyMember = false;
		final ArrayList<RelationMember> parentChildMembers = new ArrayList<RelationMember>();
		for (RelationMember relationMember : metaData.getRelationMembers())
		{
			if (relationMember.getAnnotation(ParentChild.class) != null)
			{
				parentChildMembers.add(relationMember);
				if (relationMember.isToMany())
				{
					atLeastOneToManyMember = true;
				}
			}
		}
		final boolean fAtLeastOneToManyMember = atLeastOneToManyMember;
		PropertyInstance p_toBeUpdated = implementProperty(template_p_toBeUpdated, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				if (parentChildMembers.size() == 0)
				{
					mg.getThisField(f_toBeUpdated);
					mg.returnValue();
				}
				else
				{
					int loc_iterator = -1;
					if (fAtLeastOneToManyMember)
					{
						loc_iterator = mg.newLocal(Iterator.class);
					}
					// we have to check the toBeUpdated-State for our "parentChild" members to decide our own toBeUpdate-State by OR-concatenation
					int loc_parentChildValue = mg.newLocal(Object.class);
					Label trueLabel = mg.newLabel();

					mg.getThisField(f_toBeUpdated);
					mg.ifZCmp(GeneratorAdapter.NE, trueLabel);

					for (RelationMember parentChildMember : parentChildMembers)
					{
						int relationIndex = metaData.getIndexByRelationName(parentChildMember.getName());
						Label l_valueIsNull = mg.newLabel();
						// load this RelationMember at runtime to be able to call its "getValue(Object obj)"

						mg.loadThis();
						mg.push(relationIndex);

						mg.invokeVirtual(MethodInstance.findByTemplate(RelationsGetterVisitor.m_template_isInitialized_Member, false));

						mg.ifZCmp(GeneratorAdapter.EQ, l_valueIsNull); // skip this member if it is not initialized

						mg.loadThis();
						mg.push(relationIndex);
						mg.invokeVirtual(MethodInstance.findByTemplate(RelationsGetterVisitor.m_template_getValueDirect_Member, false));

						mg.storeLocal(loc_parentChildValue);

						mg.loadLocal(loc_parentChildValue);
						mg.ifNull(l_valueIsNull);

						mg.loadLocal(loc_parentChildValue);

						if (parentChildMember.isToMany())
						{
							Label l_startLoop = mg.newLabel();
							Label l_endLoop = mg.newLabel();

							mg.checkCast(Collection.class);
							mg.invokeInterface(new MethodInstance(null, Collection.class, Iterator.class, "iterator"));
							mg.storeLocal(loc_iterator);

							mg.mark(l_startLoop);
							mg.loadLocal(loc_iterator);
							mg.invokeInterface(new MethodInstance(null, Iterator.class, boolean.class, "hasNext"));

							mg.ifZCmp(GeneratorAdapter.EQ, l_endLoop);
							mg.loadLocal(loc_iterator);
							mg.invokeInterface(new MethodInstance(null, Iterator.class, Object.class, "next"));

							mg.checkCast(IDataObject.class);
							mg.invokeInterface(template_p_toBeUpdated.getGetter());
							mg.ifZCmp(GeneratorAdapter.NE, trueLabel);

							mg.goTo(l_startLoop);
							mg.mark(l_endLoop);
						}
						else
						{
							mg.checkCast(IDataObject.class);
							mg.invokeInterface(template_p_toBeUpdated.getGetter());
							mg.ifZCmp(GeneratorAdapter.NE, trueLabel);
						}
						mg.mark(l_valueIsNull);
					}

					mg.push(false);
					mg.returnValue();

					mg.mark(trueLabel);
					mg.push(true);
					mg.returnValue();
				}
			}
		}, new Script()
		{
			@Override
			public void execute(MethodGenerator mv)
			{
				int loc_existingValue = mv.newLocal(boolean.class);
				Label l_finish = mv.newLabel();
				mv.getThisField(f_toBeUpdated);
				mv.storeLocal(loc_existingValue);

				mv.loadLocal(loc_existingValue);
				mv.loadArg(0);
				mv.ifCmp(boolean.class, GeneratorAdapter.EQ, l_finish);

				mv.putThisField(f_toBeUpdated, new Script()
				{

					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadArg(0);
					}
				});

				// call dataObjectTemplate
				mv.callThisGetter(p_dataObjectTemplate);
				// "this" argument
				mv.loadThis();
				// oldValue argument
				mv.loadLocal(loc_existingValue);
				// newValue argument
				mv.loadArg(0);
				mv.invokeVirtual(m_toBeUpdatedChanged);

				mv.mark(l_finish);
				mv.returnValue();
			}
		});
		p_toBeUpdated.addAnnotation(c_ignoreToBeUpdated);
		return p_toBeUpdated;
	}

	/**
	 * public boolean isToBeCreated() { return get__Id() == null; }
	 * 
	 * @param owner
	 */
	protected PropertyInstance implementToBeCreated(PropertyInstance p_toBeCreated)
	{
		MethodGenerator mg = visitMethod(p_toBeCreated.getGetter());
		p_toBeCreated = PropertyInstance.findByTemplate(p_toBeCreated, false);
		Member idMember = metaData.getIdMember();
		if (idMember instanceof CompositeIdMember)
		{
			ArrayList<String> names = new ArrayList<String>();
			for (Member itemMember : ((CompositeIdMember) idMember).getMembers())
			{
				names.add(itemMember.getName());
			}
			p_toBeCreated.addAnnotation(c_fireThisOPC, new Object[] { names.toArray(String.class) });
		}
		else
		{
			p_toBeCreated.addAnnotation(c_fireThisOPC, idMember.getName());
		}

		Label trueLabel = mg.newLabel();

		mg.loadThis();
		mg.invokeVirtual(GetIdMethodCreator.getGetId());

		mg.ifNull(trueLabel);

		mg.push(false);
		mg.returnValue();

		mg.mark(trueLabel);

		mg.push(true);
		mg.returnValue();
		mg.endMethod();
		return p_toBeCreated;
	}

	/**
	 * public boolean hasPendingChanges() { return isToBeUpdated() || isToBeCreated() || isToBeDeleted(); }
	 */
	protected void implementHasPendingChanges(final PropertyInstance p_hasPendingChanges, final PropertyInstance p_ToBeUpdated,
			final PropertyInstance p_ToBeCreated, final PropertyInstance p_ToBeDeleted)
	{
		setPropertyContext(p_hasPendingChanges.getName(), new IResultingBackgroundWorkerDelegate<Object>()
		{
			@Override
			public Object invoke() throws Throwable
			{
				MethodGenerator mg = visitMethod(p_hasPendingChanges.getGetter());
				PropertyInstance p_hasPendingChanges = PropertyInstance.findByTemplate(DataObjectVisitor.p_hasPendingChanges, false);
				p_hasPendingChanges.addAnnotation(c_ignoreToBeUpdated);
				p_hasPendingChanges.addAnnotation(c_fireThisOPC,
						new Object[] { new String[] { p_ToBeCreated.getName(), p_ToBeUpdated.getName(), p_ToBeDeleted.getName() } });
				// p_hasPendingChanges.addAnnotation(c_ftopc, p_ToBeUpdated.getName());
				// p_hasPendingChanges.addAnnotation(c_ftopc, p_ToBeDeleted.getName());

				Label trueLabel = mg.newLabel();

				mg.loadThis();
				mg.invokeVirtual(p_ToBeUpdated.getGetter());
				mg.ifZCmp(GeneratorAdapter.NE, trueLabel);

				mg.loadThis();
				mg.invokeVirtual(p_ToBeCreated.getGetter());
				mg.ifZCmp(GeneratorAdapter.NE, trueLabel);

				mg.loadThis();
				mg.invokeVirtual(p_ToBeDeleted.getGetter());
				mg.ifZCmp(GeneratorAdapter.NE, trueLabel);

				mg.push(false);
				mg.returnValue();

				mg.mark(trueLabel);
				mg.push(true);
				mg.returnValue();
				mg.endMethod();
				return null;
			}
		});
	}
}
