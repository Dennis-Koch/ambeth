package de.osthus.ambeth.bytecode.visitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IValueResolveDelegate;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.TypeUtil;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.util.EnhancerUtil;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.collections.specialized.INotifyCollectionChangedListener;
import de.osthus.ambeth.collections.specialized.NotifyCollectionChangedEvent;
import de.osthus.ambeth.collections.specialized.PropertyChangeSupport;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.model.INotifyPropertyChangedSource;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.template.PropertyChangeTemplate;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

/**
 * NotifyPropertyChangedMethodVisitor implements {@link INotifyPropertyChanged} and invokes {@link PropertyChangeListener#propertyChanged} when a property is
 * changed. If the enhanced object implements {@link PropertyChangeListener} it is registered using
 * {@link INotifyPropertyChanged#addPropertyChangeListener(PropertyChangeListener)}
 */
public class NotifyPropertyChangedClassVisitor extends ClassGenerator
{
	public static final Class<?> templateType = PropertyChangeTemplate.class;

	protected static final String templatePropertyName = templateType.getSimpleName();

	public static final MethodInstance template_m_collectionChanged = new MethodInstance(null, INotifyCollectionChangedListener.class, "collectionChanged",
			NotifyCollectionChangedEvent.class);

	public static final MethodInstance template_m_PropertyChanged = new MethodInstance(null, PropertyChangeListener.class, "propertyChange",
			PropertyChangeEvent.class);

	public static final MethodInstance template_m_onPropertyChanged = new MethodInstance(null, INotifyPropertyChangedSource.class, "onPropertyChanged",
			String.class);

	public static final MethodInstance template_m_onPropertyChanged_Values = new MethodInstance(null, INotifyPropertyChangedSource.class, "onPropertyChanged",
			String.class, Object.class, Object.class);

	public static final MethodInstance m_handlePropertyChange = new MethodInstance(null, templateType, "handleParentChildPropertyChange",
			INotifyPropertyChangedSource.class, PropertyChangeEvent.class);

	public static final MethodInstance m_handleCollectionChange = new MethodInstance(null, templateType, "handleCollectionChange",
			INotifyPropertyChangedSource.class, NotifyCollectionChangedEvent.class);

	protected static final MethodInstance m_newPropertyChangeSupport = new MethodInstance(null, templateType, "newPropertyChangeSupport", Object.class);

	protected static final MethodInstance m_getMethodHandle = new MethodInstance(null, templateType, "getMethodHandle", INotifyPropertyChangedSource.class,
			String.class);

	protected static final MethodInstance m_firePropertyChange = new MethodInstance(null, templateType, "firePropertyChange",
			INotifyPropertyChangedSource.class, PropertyChangeSupport.class, IPropertyInfo.class, Object.class, Object.class);

	protected static final MethodInstance m_addPropertyChangeListener = new MethodInstance(null, templateType, "addPropertyChangeListener",
			PropertyChangeSupport.class, PropertyChangeListener.class);

	protected static final MethodInstance m_removePropertyChangeListener = new MethodInstance(null, templateType, "removePropertyChangeListener",
			PropertyChangeSupport.class, PropertyChangeListener.class);

	public static final MethodInstance template_m_firePropertyChange = new MethodInstance(null, Opcodes.ACC_PROTECTED, "firePropertyChange", null, void.class,
			PropertyChangeSupport.class, IPropertyInfo.class, Object.class, Object.class);

	protected static final MethodInstance template_m_getPropertyChangeSupport = new MethodInstance(null, Opcodes.ACC_PUBLIC,
			"use$PropertyChangeSupport", null, PropertyChangeSupport.class);

	public static final PropertyInstance p_propertyChangeSupport = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class, "PropertyChangeSupport",
			false);

	public static final PropertyInstance p_parentChildEventHandler = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class,
			"ParentChildEventHandler", false);

	public static final PropertyInstance p_collectionEventHandler = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class,
			"CollectionEventHandler", false);

	public static String getPropertyNameForGetterMethodHandle(String propertyName)
	{
		return propertyName + "$GetterHandle";
	}

	public static PropertyInstance getPropertyChangeTemplatePI(ClassGenerator cv)
	{
		PropertyInstance pi = getState().getProperty(templatePropertyName);
		if (pi != null)
		{
			return pi;
		}
		Object bean = getState().getBeanContext().getService(templateType);
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	/** property infos of enhanced type */
	protected String[] properties;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	public NotifyPropertyChangedClassVisitor(ClassVisitor cv, String[] properties)
	{
		super(cv);
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd()
	{
		FieldInstance f_propertyChangeSupport = getPropertyChangeSupportField();
		PropertyInstance p_propertyChangeTemplate = getPropertyChangeTemplatePI(this);

		MethodInstance m_getPropertyChangeSupport = implementGetPropertyChangeSupport(p_propertyChangeTemplate, f_propertyChangeSupport);

		implementNotifyPropertyChanged(p_propertyChangeTemplate, m_getPropertyChangeSupport);

		MethodInstance m_firePropertyChange = implementFirePropertyChange(p_propertyChangeTemplate);

		implementNotifyPropertyChangedSource(p_propertyChangeTemplate, f_propertyChangeSupport);

		if (properties == null)
		{
			implementCollectionChanged(p_propertyChangeTemplate);
			implementPropertyChanged(p_propertyChangeTemplate);

			// handle all properties found
			IPropertyInfo[] props = propertyInfoProvider.getProperties(getState().getCurrentType());
			for (IPropertyInfo prop : props)
			{
				if (prop.getName().endsWith(ValueHolderIEC.getNoInitSuffix()))
				{
					continue;
				}
				PropertyInstance propInfo = PropertyInstance.findByTemplate(prop.getName(), true);
				if (propInfo == null)
				{
					continue;
				}
				implementPropertyChangeOnProperty(propInfo, m_firePropertyChange, f_propertyChangeSupport);
			}
		}
		else
		{
			for (String propertyName : properties)
			{
				PropertyInstance propInfo = PropertyInstance.findByTemplate(propertyName, false);
				implementPropertyChangeOnProperty(propInfo, m_firePropertyChange, f_propertyChangeSupport);
			}
		}
		super.visitEnd();
	}

	protected void implementPropertyChangeOnProperty(final PropertyInstance propertyInfo, MethodInstance m_firePropertyChange,
			FieldInstance f_propertyChangeSupport)
	{
		// add property change detection and notification
		if (propertyInfo.getGetter() == null || propertyInfo.getSetter() == null)
		{
			return;
		}
		PropertyInstance p_setterMethodHandle = implementAssignedReadonlyProperty(getPropertyNameForGetterMethodHandle(propertyInfo.getName()),
				new IValueResolveDelegate()
				{
					@Override
					public Class<?> getValueType()
					{
						return IPropertyInfo.class;
					}

					@Override
					public Object invoke(String fieldName, Class<?> enhancedType)
					{
						return propertyInfoProvider.getProperty(enhancedType, propertyInfo.getName());
					}
				});
		Type propertyType = propertyInfo.getPropertyType();

		MethodGenerator mg = visitMethod(propertyInfo.getSetter());
		Label l_finish = mg.newLabel();
		Label l_noOldValue = mg.newLabel();
		Label l_noChangeCheck = mg.newLabel();
		int loc_oldValue = mg.newLocal(propertyType);
		int loc_valueChanged = mg.newLocal(boolean.class);

		MethodInstance m_getSuper = EnhancerUtil.getSuperGetter(propertyInfo);
		boolean relationProperty = m_getSuper.getName().endsWith(ValueHolderIEC.getNoInitSuffix());

		// initialize flag with false
		mg.push(false);
		mg.storeLocal(loc_valueChanged);

		// initialize oldValue with null
		mg.pushNullOrZero(propertyType);
		mg.storeLocal(loc_oldValue);

		if (relationProperty)
		{
			// check if a setter call to an UNINITIALIZED relation occured with value null
			// if it the case there would be no PCE because oldValue & newValue are both null
			// but we need a PCE in this special case
			Label l_noSpecialHandling = mg.newLabel();
			FieldInstance f_state = getState().getAlreadyImplementedField(ValueHolderIEC.getInitializedFieldName(propertyInfo.getName()));
			mg.getThisField(f_state);
			mg.pushEnum(ValueHolderState.INIT);
			mg.ifCmp(f_state.getType(), GeneratorAdapter.EQ, l_noSpecialHandling);
			mg.push(true);
			mg.storeLocal(loc_valueChanged);
			mg.mark(l_noSpecialHandling);
		}

		// check if value should be checked to decide for a PCE
		mg.loadLocal(loc_valueChanged);
		mg.ifZCmp(GeneratorAdapter.NE, l_noOldValue);

		// get old field value calling super property getter
		mg.loadThis();
		mg.invokeOnExactOwner(m_getSuper);
		mg.storeLocal(loc_oldValue);

		mg.mark(l_noOldValue);

		// set new field value calling super property setter
		mg.loadThis();
		mg.loadArg(0);
		mg.invokeOnExactOwner(EnhancerUtil.getSuperSetter(propertyInfo));
		mg.popIfReturnValue(EnhancerUtil.getSuperSetter(propertyInfo));

		// check if value should be checked to decide for a PCE
		mg.loadLocal(loc_valueChanged);
		mg.ifZCmp(GeneratorAdapter.NE, l_noChangeCheck);

		// compare field values
		mg.loadLocal(loc_oldValue);
		mg.loadArg(0);
		mg.ifCmp(propertyType, GeneratorAdapter.EQ, l_finish);

		mg.mark(l_noChangeCheck);
		// call firePropertyChange on this
		mg.loadThis();
		// propertyChangeSupport
		mg.getThisField(f_propertyChangeSupport);
		// property
		mg.callThisGetter(p_setterMethodHandle);
		// oldValue
		mg.loadLocal(loc_oldValue);
		if (Type.BOOLEAN_TYPE.equals(propertyType))
		{
			mg.invokeStatic(new MethodInstance(null, Boolean.class, "valueOf", boolean.class));
		}
		else if (TypeUtil.isPrimitive(propertyType))
		{
			mg.box(propertyType);
		}
		// newValue
		mg.loadArg(0);
		if (Type.BOOLEAN_TYPE.equals(propertyType))
		{
			mg.invokeStatic(new MethodInstance(null, Boolean.class, "valueOf", boolean.class));
		}
		else if (TypeUtil.isPrimitive(propertyType))
		{
			mg.box(propertyType);
		}
		// firePropertyChange(propertyChangeSupport, property, oldValue, newValue)
		mg.invokeVirtual(m_firePropertyChange);

		// return
		mg.mark(l_finish);
		mg.returnVoidOrThis();
		mg.endMethod();
	}

	protected void implementCollectionChanged(PropertyInstance p_propertyChangeTemplate)
	{
		MethodInstance m_collectionChanged_super = MethodInstance.findByTemplate(template_m_collectionChanged, true);

		MethodGenerator mv = visitMethod(template_m_collectionChanged);
		if (m_collectionChanged_super != null)
		{
			mv.loadThis();
			mv.loadArgs();
			mv.invokeSuperOfCurrentMethod();
		}
		mv.callThisGetter(p_propertyChangeTemplate);
		mv.loadThis();
		mv.loadArgs();
		// call PCT.HandleCollectionChange(this, arg)
		mv.invokeVirtual(m_handleCollectionChange);
		mv.returnValue();
		mv.endMethod();
	}

	protected void implementPropertyChanged(PropertyInstance p_propertyChangeTemplate)
	{
		MethodInstance m_propertyChanged_super = MethodInstance.findByTemplate(template_m_PropertyChanged, true);
		MethodGenerator mv = visitMethod(template_m_PropertyChanged);
		if (m_propertyChanged_super != null)
		{
			mv.loadThis();
			mv.loadArgs();
			mv.invokeSuperOfCurrentMethod();
		}
		mv.callThisGetter(p_propertyChangeTemplate);
		mv.loadThis();
		mv.loadArgs();
		// call PCT.HandleParentChildPropertyChange(this, evnt)
		mv.invokeVirtual(m_handlePropertyChange);
		mv.returnValue();
		mv.endMethod();
	}

	/**
	 * Almost empty implementation (just calling the static method) to be able to override and act on the property change
	 */
	protected MethodInstance implementFirePropertyChange(PropertyInstance p_propertyChangeTemplate)
	{
		MethodInstance m_firePropertyChange_super = MethodInstance.findByTemplate(template_m_firePropertyChange, true);

		MethodGenerator mg;
		if (m_firePropertyChange_super == null)
		{
			// implement new
			mg = visitMethod(template_m_firePropertyChange);
		}
		else
		{
			// override existing
			mg = visitMethod(m_firePropertyChange_super);
		}
		mg.callThisGetter(p_propertyChangeTemplate);
		mg.loadThis();
		mg.loadArgs();
		// firePropertyChange(thisPointer, propertyChangeSupport, property, oldValue, newValue)
		mg.invokeVirtual(m_firePropertyChange);
		mg.popIfReturnValue(m_firePropertyChange);
		mg.returnVoidOrThis();
		mg.endMethod();

		return mg.getMethod();
	}

	protected FieldInstance getPropertyChangeSupportField()
	{
		FieldInstance f_propertyChangeSupport = BytecodeBehaviorState.getState().getAlreadyImplementedField("$propertyChangeSupport");
		if (f_propertyChangeSupport == null)
		{
			f_propertyChangeSupport = new FieldInstance(Opcodes.ACC_PROTECTED, "$propertyChangeSupport", null, PropertyChangeSupport.class);
		}
		return f_propertyChangeSupport;
	}

	protected MethodInstance implementGetPropertyChangeSupport(final PropertyInstance p_propertyChangeTemplate, FieldInstance f_propertyChangeSupport)
	{
		MethodInstance m_getPropertyChangeSupport = MethodInstance.findByTemplate(template_m_getPropertyChangeSupport, true);

		if (m_getPropertyChangeSupport == null)
		{
			// create field that holds propertyChangeSupport
			f_propertyChangeSupport = implementField(f_propertyChangeSupport);
			MethodGenerator mg = visitMethod(template_m_getPropertyChangeSupport);
			Label l_pcsValid = mg.newLabel();
			mg.getThisField(f_propertyChangeSupport);
			mg.dup();
			mg.ifNonNull(l_pcsValid);

			mg.pop(); // remove 2nd null instance from stack caused by previous dup
			mg.putThisField(f_propertyChangeSupport, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.callThisGetter(p_propertyChangeTemplate);
					mg.loadThis();
					mg.invokeVirtual(m_newPropertyChangeSupport);
				}
			});
			mg.getThisField(f_propertyChangeSupport);

			mg.mark(l_pcsValid);
			mg.returnValue(); // return instance already on the stack by both branches
			mg.endMethod();

			m_getPropertyChangeSupport = mg.getMethod();
		}
		return m_getPropertyChangeSupport;
	}

	protected PropertyInstance implementNotifyPropertyChangedSource(PropertyInstance p_propertyChangeTemplate, FieldInstance f_propertyChangeSupport)
	{
		MethodInstance m_onPropertyChanged_Values = MethodInstance.findByTemplate(template_m_onPropertyChanged_Values, true);
		if (m_onPropertyChanged_Values == null)
		{
			MethodGenerator mv = visitMethod(template_m_onPropertyChanged_Values);
			mv.callThisGetter(p_propertyChangeTemplate);
			mv.loadThis();
			mv.getThisField(f_propertyChangeSupport);

			// getSetterHandle(sender, propertyName)
			mv.callThisGetter(p_propertyChangeTemplate);
			mv.loadThis();
			mv.loadArg(0);
			mv.invokeVirtual(m_getMethodHandle);

			mv.loadArg(0);
			mv.loadArg(1);
			// firePropertyChange(sender, propertyChangeSupport, property, oldValue, newValue)
			mv.invokeVirtual(m_firePropertyChange);
			mv.popIfReturnValue(m_firePropertyChange);
			mv.returnVoidOrThis();
			mv.endMethod();
			m_onPropertyChanged_Values = mv.getMethod();
		}
		MethodInstance m_onPropertyChanged = MethodInstance.findByTemplate(template_m_onPropertyChanged, true);
		if (m_onPropertyChanged == null)
		{
			MethodGenerator mv = visitMethod(template_m_onPropertyChanged);
			mv.loadThis();
			mv.loadArg(0);
			mv.pushNull();
			mv.pushNull();
			mv.invokeVirtual(m_onPropertyChanged_Values);
			mv.popIfReturnValue(m_onPropertyChanged_Values);
			mv.returnVoidOrThis();
			mv.endMethod();
			m_onPropertyChanged = mv.getMethod();
		}
		PropertyInstance p_pceHandlers = PropertyInstance.findByTemplate(p_propertyChangeSupport, true);
		if (p_pceHandlers == null)
		{
			implementGetter(p_propertyChangeSupport.getGetter(), f_propertyChangeSupport);
			p_pceHandlers = PropertyInstance.findByTemplate(p_propertyChangeSupport, false);
		}
		if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext()))
		{
			PropertyInstance p_parentEntity = EmbeddedTypeVisitor.getParentEntityProperty(this);
			if (MethodInstance.findByTemplate(p_parentChildEventHandler.getGetter(), true) == null)
			{
				MethodGenerator mv = visitMethod(p_parentChildEventHandler.getGetter());
				mv.callThisGetter(p_parentEntity);
				mv.invokeInterface(p_parentChildEventHandler.getGetter());
				mv.returnValue();
				mv.endMethod();
			}
			if (MethodInstance.findByTemplate(p_collectionEventHandler.getGetter(), true) == null)
			{
				MethodGenerator mv = visitMethod(p_collectionEventHandler.getGetter());
				mv.callThisGetter(p_parentEntity);
				mv.invokeInterface(p_collectionEventHandler.getGetter());
				mv.returnValue();
				mv.endMethod();
			}
		}
		else
		{
			if (MethodInstance.findByTemplate(p_parentChildEventHandler.getGetter(), true) == null)
			{
				implementProperty(p_parentChildEventHandler, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadThis();
						mg.returnValue();
					}
				}, null);
			}
			if (MethodInstance.findByTemplate(p_collectionEventHandler.getGetter(), true) == null)
			{
				implementProperty(p_collectionEventHandler, new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadThis();
						mg.returnValue();
					}
				}, null);
			}
		}
		return p_pceHandlers;
	}

	protected void implementNotifyPropertyChanged(PropertyInstance p_propertyChangeTemplate, MethodInstance m_getPropertyChangeSupport)
	{
		// implement IPropertyChanged
		for (java.lang.reflect.Method rMethod : INotifyPropertyChanged.class.getMethods())
		{
			MethodInstance existingMethod = MethodInstance.findByTemplate(rMethod, true);
			if (existingMethod != null)
			{
				continue;
			}
			MethodInstance method = new MethodInstance(rMethod);
			MethodGenerator mg = visitMethod(method);
			mg.callThisGetter(p_propertyChangeTemplate);
			// this.propertyChangeSupport
			mg.callThisGetter(m_getPropertyChangeSupport);
			// listener
			mg.loadArg(0);
			if ("addPropertyChangeListener".equals(method.getName()))
			{
				// addPropertyChangeListener(propertyChangeSupport, listener)
				mg.invokeVirtual(m_addPropertyChangeListener);
			}
			else
			{
				// removePropertyChangeListener(propertyChangeSupport, listener)
				mg.invokeVirtual(m_removePropertyChangeListener);
			}
			mg.returnValue();
			mg.endMethod();
		}
	}
}
