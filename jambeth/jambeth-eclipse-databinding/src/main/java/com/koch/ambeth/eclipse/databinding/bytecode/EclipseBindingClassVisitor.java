package com.koch.ambeth.eclipse.databinding.bytecode;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.cache.bytecode.visitor.EmbeddedTypeVisitor;
import com.koch.ambeth.eclipse.databinding.IListChangeListenerSource;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class EclipseBindingClassVisitor extends ClassGenerator {
	public static final Class<?> templateType = EclipseBindingMixin.class;

	protected static final String templatePropertyName = "__" + templateType.getSimpleName();

	public static final MethodInstance template_m_collectionChanged =
			new MethodInstance(null, ReflectUtil.getDeclaredMethod(false, IListChangeListener.class,
					void.class, "handleListChange", ListChangeEvent.class),
					"(L" + Type.getType(ListChangeEvent.class).getInternalName() + "<+"
							+ Type.getDescriptor(Object.class)
							+ ">;)V");

	public static final MethodInstance m_handleCollectionChange =
			new MethodInstance(null, templateType, void.class, "handleListChange",
					Object.class, ListChangeEvent.class);

	public static final PropertyInstance p_collectionEventHandler =
			PropertyInstance.findByTemplate(IListChangeListenerSource.class, "ListChangeListener",
					IListChangeListener.class, false);

	public static PropertyInstance getEclipseBindingTemplatePI(ClassGenerator cv) {
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
		if (pi != null) {
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	/** property infos of enhanced type */
	protected String[] properties;

	protected IEntityMetaData metaData;

	public EclipseBindingClassVisitor(ClassVisitor cv, IEntityMetaData metaData,
			String[] properties) {
		super(cv);
		this.metaData = metaData;
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd() {
		PropertyInstance p_propertyChangeTemplate = getEclipseBindingTemplatePI(this);

		implementNotifyPropertyChangedSource(p_propertyChangeTemplate);

		if (properties == null) {
			implementCollectionChanged(p_propertyChangeTemplate);
		}
		super.visitEnd();
	}

	protected void implementCollectionChanged(PropertyInstance p_propertyChangeTemplate) {
		MethodInstance m_collectionChanged_super =
				MethodInstance.findByTemplate(template_m_collectionChanged, true);

		MethodGenerator mv = visitMethod(template_m_collectionChanged);
		if (m_collectionChanged_super != null) {
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

	protected void implementNotifyPropertyChangedSource(PropertyInstance p_propertyChangeTemplate) {
		if (EmbeddedEnhancementHint.hasMemberPath(getState().getContext())) {
			PropertyInstance p_parentEntity = EmbeddedTypeVisitor.getParentEntityProperty(this);
			if (MethodInstance.findByTemplate(p_collectionEventHandler.getGetter(), true) == null) {
				MethodGenerator mv = visitMethod(p_collectionEventHandler.getGetter());
				mv.callThisGetter(p_parentEntity);
				mv.invokeInterface(p_collectionEventHandler.getGetter());
				mv.returnValue();
				mv.endMethod();
			}
		}
		else {
			if (MethodInstance.findByTemplate(p_collectionEventHandler.getGetter(), true) == null) {
				implementProperty(p_collectionEventHandler, new Script() {
					@Override
					public void execute(MethodGenerator mg) {
						mg.loadThis();
						mg.returnValue();
					}
				}, null);
			}
		}
	}
}
