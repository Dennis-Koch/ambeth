package com.koch.ambeth.cache.bytecode.visitor;

/*-
 * #%L
 * jambeth-cache-bytecode
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.IValueResolveDelegate;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.cache.ValueHolderIEC;
import com.koch.ambeth.cache.bytecode.util.EnhancerUtil;
import com.koch.ambeth.cache.mixin.PropertyChangeMixin;
import com.koch.ambeth.cache.proxy.IPropertyChangeConfigurable;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedEvent;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

/**
 * NotifyPropertyChangedMethodVisitor implements {@link INotifyPropertyChanged} and invokes {@link PropertyChangeListener#propertyChanged} when a property is
 * changed. If the enhanced object implements {@link PropertyChangeListener} it is registered using
 * {@link INotifyPropertyChanged#addPropertyChangeListener(PropertyChangeListener)}
 */
public class NotifyPropertyChangedClassVisitor extends ClassGenerator
{
	public static final Class<?> templateType = PropertyChangeMixin.class;

	protected static final String templatePropertyName = "__" + templateType.getSimpleName();

	public static final MethodInstance template_m_collectionChanged = new MethodInstance(null, INotifyCollectionChangedListener.class, void.class,
			"collectionChanged", NotifyCollectionChangedEvent.class);

	public static final MethodInstance template_m_PropertyChanged = new MethodInstance(null, PropertyChangeListener.class, void.class, "propertyChange",
			PropertyChangeEvent.class);

	public static final MethodInstance template_m_onPropertyChanged = new MethodInstance(null, INotifyPropertyChangedSource.class, void.class,
			"onPropertyChanged", String.class);

	public static final MethodInstance template_m_onPropertyChanged_Values = new MethodInstance(null, INotifyPropertyChangedSource.class, void.class,
			"onPropertyChanged", String.class, Object.class, Object.class);

	public static final MethodInstance template_m_isPropertyChangeActive = new MethodInstance(null, IPropertyChangeConfigurable.class, boolean.class,
			"is__PropertyChangeActive");

	public static final MethodInstance template_m_setPropertyChangeActive = new MethodInstance(null, IPropertyChangeConfigurable.class, void.class,
			"set__PropertyChangeActive", boolean.class);

	public static final MethodInstance m_handlePropertyChange = new MethodInstance(null, templateType, void.class, "handleParentChildPropertyChange",
			INotifyPropertyChangedSource.class, PropertyChangeEvent.class);

	public static final MethodInstance m_handleCollectionChange = new MethodInstance(null, templateType, void.class, "handleCollectionChange",
			INotifyPropertyChangedSource.class, NotifyCollectionChangedEvent.class);

	protected static final MethodInstance m_newPropertyChangeSupport = new MethodInstance(null, templateType, PropertyChangeSupport.class,
			"newPropertyChangeSupport", Object.class);

	protected static final MethodInstance m_getMethodHandle = new MethodInstance(null, templateType, IPropertyInfo.class, "getMethodHandle",
			INotifyPropertyChangedSource.class, String.class);

	protected static final MethodInstance m_firePropertyChange = new MethodInstance(null, templateType, void.class, "firePropertyChange",
			INotifyPropertyChangedSource.class, PropertyChangeSupport.class, IPropertyInfo.class, Object.class, Object.class);

	protected static final MethodInstance m_addPropertyChangeListener = new MethodInstance(null, templateType, void.class, "addPropertyChangeListener",
			PropertyChangeSupport.class, PropertyChangeListener.class);

	protected static final MethodInstance m_removePropertyChangeListener = new MethodInstance(null, templateType, void.class, "removePropertyChangeListener",
			PropertyChangeSupport.class, PropertyChangeListener.class);

	public static final MethodInstance template_m_firePropertyChange = new MethodInstance(null, Opcodes.ACC_PROTECTED, void.class, "firePropertyChange", null,
			PropertyChangeSupport.class, IPropertyInfo.class, Object.class, Object.class);

	protected static final MethodInstance template_m_usePropertyChangeSupport = new MethodInstance(null, Opcodes.ACC_PUBLIC, PropertyChangeSupport.class,
			"use$PropertyChangeSupport", null);

	public static final PropertyInstance template_p_propertyChangeSupport = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class,
			"PropertyChangeSupport", PropertyChangeSupport.class, false);

	public static final PropertyInstance p_parentChildEventHandler = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class,
			"ParentChildEventHandler", PropertyChangeListener.class, false);

	public static final PropertyInstance p_collectionEventHandler = PropertyInstance.findByTemplate(INotifyPropertyChangedSource.class,
			"CollectionEventHandler", INotifyCollectionChangedListener.class, false);

	public static String getPropertyNameForGetterMethodHandle(String propertyName)
	{
		return propertyName + "$GetterHandle";
	}

	public static PropertyInstance getPropertyChangeTemplatePI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
		if (pi != null)
		{
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	/** property infos of enhanced type */
	protected String[] properties;

	protected IEntityMetaData metaData;

	public NotifyPropertyChangedClassVisitor(ClassVisitor cv, IEntityMetaData metaData, String[] properties)
	{
		super(cv);
		this.metaData = metaData;
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd()
	{
		PropertyInstance p_propertyChangeTemplate = getPropertyChangeTemplatePI(this);

		implementPropertyChangeConfigurable();

		PropertyInstance p_propertyChangeSupport = PropertyInstance.findByTemplate(template_p_propertyChangeSupport, true);
		if (p_propertyChangeSupport == null)
		{
			FieldInstance f_propertyChangeSupport = getPropertyChangeSupportField();
			if (BytecodeBehaviorState.getState().getAlreadyImplementedField(f_propertyChangeSupport.getName()) == null)
			{
				f_propertyChangeSupport = implementField(f_propertyChangeSupport);
			}
			p_propertyChangeSupport = implementGetter(template_p_propertyChangeSupport, f_propertyChangeSupport);

			if (template_p_propertyChangeSupport.getSetter() == null)
			{
				p_propertyChangeSupport = implementSetter(template_p_propertyChangeSupport, f_propertyChangeSupport);
			}
		}
		MethodInstance m_usePropertyChangeSupport = implementUsePropertyChangeSupport(p_propertyChangeTemplate, p_propertyChangeSupport);

		implementNotifyPropertyChanged(p_propertyChangeTemplate, m_usePropertyChangeSupport);

		MethodInstance m_firePropertyChange = implementFirePropertyChange(p_propertyChangeTemplate);

		implementNotifyPropertyChangedSource(p_propertyChangeTemplate, p_propertyChangeSupport);

		if (properties == null)
		{
			// implementSelfAsListener();

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
				PropertyInstance propInfo = PropertyInstance.findByTemplate(prop.getName(), prop.getPropertyType(), true);
				if (propInfo == null)
				{
					continue;
				}
				implementPropertyChangeOnProperty(propInfo, m_firePropertyChange, p_propertyChangeSupport);
			}
		}
		else
		{
			for (String propertyName : properties)
			{
				PropertyInstance propInfo = PropertyInstance.findByTemplate(propertyName, (Type) null, false);
				implementPropertyChangeOnProperty(propInfo, m_firePropertyChange, p_propertyChangeSupport);
			}
		}
		super.visitEnd();
	}

	protected void implementSelfAsListener()
	{
		Class<?> currentType = getState().getCurrentType();
		Constructor<?>[] constructors = currentType.getDeclaredConstructors();

		for (Constructor<?> constructor : constructors)
		{
			ConstructorInstance c_method = new ConstructorInstance(constructor);

			MethodGenerator mg = visitMethod(c_method);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeConstructor(c_method);

			// if (PropertyChangeListener.class.isAssignableFrom(getClass()))
			mg.ifThisInstanceOf(PropertyChangeListener.class, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadThis();
				}
			}, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					MethodInstance m_addPropertyChangeListener = MethodInstance.findByTemplate(false, void.class, "addPropertyChangeListener",
							PropertyChangeListener.class);
					mg.loadThis();
					mg.dup();
					mg.invokeVirtual(m_addPropertyChangeListener);
					//
					// mg.callThisGetter(m_usePropertyChangeSupport);
					// fgfg
					// mg.pop();
				}
			}, null);

			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementPropertyChangeOnProperty(final PropertyInstance propertyInfo, MethodInstance m_firePropertyChange,
			PropertyInstance p_propertyChangeSupport)
	{
		// add property change detection and notification
		if (propertyInfo.getGetter() == null || propertyInfo.getSetter() == null)
		{
			return;
		}
		if (metaData != null && InitializeEmbeddedMemberVisitor.isEmbeddedMember(metaData, propertyInfo.getName()))
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
			// if it is the case there would be no PCE because oldValue & newValue are both null
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
		mg.callThisGetter(p_propertyChangeSupport);
		// property
		mg.callThisGetter(p_setterMethodHandle);
		// oldValue
		mg.loadLocal(loc_oldValue);
		mg.valueOf(propertyType);
		// newValue
		mg.loadArg(0);
		mg.valueOf(propertyType);
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
		Label l_propertyChangeIsInactive = mg.newLabel();

		MethodInstance m_isPropertyChangeActive = MethodInstance.findByTemplate(template_m_isPropertyChangeActive, false);

		mg.callThisGetter(m_isPropertyChangeActive);
		mg.ifZCmp(GeneratorAdapter.EQ, l_propertyChangeIsInactive);

		mg.callThisGetter(p_propertyChangeTemplate);
		mg.loadThis();
		mg.loadArgs();
		// firePropertyChange(thisPointer, propertyChangeSupport, property, oldValue, newValue)
		mg.invokeVirtual(m_firePropertyChange);
		mg.popIfReturnValue(m_firePropertyChange);
		mg.mark(l_propertyChangeIsInactive);

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

	protected MethodInstance implementUsePropertyChangeSupport(final PropertyInstance p_propertyChangeTemplate, PropertyInstance p_propertyChangeSupport)
	{
		MethodInstance m_usePropertyChangeSupport = MethodInstance.findByTemplate(template_m_usePropertyChangeSupport, true);

		if (m_usePropertyChangeSupport == null)
		{
			// create field that holds propertyChangeSupport
			MethodGenerator mg = visitMethod(template_m_usePropertyChangeSupport);
			Label l_pcsValid = mg.newLabel();
			mg.callThisGetter(p_propertyChangeSupport);
			mg.dup();
			mg.ifNonNull(l_pcsValid);

			mg.pop(); // remove 2nd null instance from stack caused by previous dup
			mg.callThisSetter(p_propertyChangeSupport, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.callThisGetter(p_propertyChangeTemplate);
					mg.loadThis();
					mg.invokeVirtual(m_newPropertyChangeSupport);
				}
			});
			mg.callThisGetter(p_propertyChangeSupport);

			mg.mark(l_pcsValid);
			mg.returnValue(); // return instance already on the stack by both branches
			mg.endMethod();

			m_usePropertyChangeSupport = mg.getMethod();
		}
		return m_usePropertyChangeSupport;
	}

	protected void implementNotifyPropertyChangedSource(PropertyInstance p_propertyChangeTemplate, PropertyInstance p_propertyChangeSupport)
	{
		MethodInstance m_onPropertyChanged_Values = MethodInstance.findByTemplate(template_m_onPropertyChanged_Values, true);
		if (m_onPropertyChanged_Values == null)
		{
			MethodGenerator mv = visitMethod(template_m_onPropertyChanged_Values);
			mv.callThisGetter(p_propertyChangeTemplate);
			mv.loadThis();
			mv.callThisGetter(p_propertyChangeSupport);

			// getSetterHandle(sender, propertyName)
			mv.callThisGetter(p_propertyChangeTemplate);
			mv.loadThis();
			mv.loadArg(0);
			mv.invokeVirtual(m_getMethodHandle);

			mv.loadArg(1);
			mv.loadArg(2);
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
	}

	protected void implementNotifyPropertyChanged(PropertyInstance p_propertyChangeTemplate, MethodInstance m_usePropertyChangeSupport)
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
			mg.callThisGetter(m_usePropertyChangeSupport);
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

	protected void implementPropertyChangeConfigurable()
	{
		String fieldName = "__propertyChangeActive";
		if (getState().getAlreadyImplementedField(fieldName) != null)
		{
			if (properties == null)
			{
				throw new IllegalStateException("It seems that this visitor has been executing twice");
			}
			return;
		}
		else if (properties != null)
		{
			// do not apply in this case
			return;
		}
		FieldInstance f_propertyChangeActive = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, fieldName, null, boolean.class));

		Constructor<?>[] constructors = BytecodeBehaviorState.getState().getCurrentType().getDeclaredConstructors();
		for (int a = constructors.length; a-- > 0;)
		{
			ConstructorInstance ci = new ConstructorInstance(constructors[a]);
			MethodGenerator mv = visitMethod(ci);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeSuperOfCurrentMethod();

			mv.putThisField(f_propertyChangeActive, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.push(true);
				}
			});
			mv.returnValue();
			mv.endMethod();
		}
		implementGetter(template_m_isPropertyChangeActive, f_propertyChangeActive);
		implementSetter(template_m_setPropertyChangeActive, f_propertyChangeActive);
	}
}
