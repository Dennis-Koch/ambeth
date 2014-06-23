package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.annotation.IgnoreToBeUpdated;
import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.repackaged.org.objectweb.asm.AnnotationVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.template.DataObjectTemplate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class DataObjectVisitor extends ClassGenerator
{
	public static final Class<?> templateType = DataObjectTemplate.class;

	protected static final String templatePropertyName = templateType.getSimpleName();

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

		// ToBeUpdated
		final FieldInstance f_toBeUpdated = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$toBeUpdated", null,
				template_p_toBeUpdated.getPropertyType()));

		final PropertyInstance p_dataObjectTemplate = getDataObjectTemplatePI(this);

		PropertyInstance p_toBeUpdated = implementProperty(template_p_toBeUpdated, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				AnnotationVisitor av = mg.visitAnnotation(Type.getDescriptor(c_ignoreToBeUpdated), true);
				av.visitEnd();
				mg.getThisField(f_toBeUpdated);
				mg.returnValue();
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

	/**
	 * public boolean isToBeCreated() { return get__Id() == null; }
	 * 
	 * @param owner
	 */
	protected PropertyInstance implementToBeCreated(PropertyInstance p_toBeCreated)
	{
		MethodGenerator mg = visitMethod(p_toBeCreated.getGetter());
		p_toBeCreated = PropertyInstance.findByTemplate(p_toBeCreated, false);
		ITypeInfoItem idMember = metaData.getIdMember();
		if (idMember instanceof CompositeIdTypeInfoItem)
		{
			ArrayList<String> names = new ArrayList<String>();
			for (ITypeInfoItem itemMember : ((CompositeIdTypeInfoItem) idMember).getMembers())
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
