package com.koch.ambeth.example.accessor;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class AccessorTypeProviderExample implements IInitializingBean {
	public static abstract class ExampleConstructorDelegate {
		public abstract Object create(int arg1);

		public abstract Object create(long arg1);
	}

	public static class ExampleObjectType {
		protected int exampleIntValue;

		protected long exampleLongValue;

		public ExampleObjectType(int exampleIntValue) {
			this.exampleIntValue = exampleIntValue;
		}

		public ExampleObjectType(long exampleLongValue) {
			this.exampleLongValue = exampleLongValue;
		}

		public int getExampleIntValue() {
			return exampleIntValue;
		}

		public long getExampleLongValue() {
			return exampleLongValue;
		}
	}

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected ExampleConstructorDelegate constructorDelegate;

	protected AbstractAccessor exampleIntAccessor, exampleLongAccessor;

	@Override
	public void afterPropertiesSet() throws Throwable {
		Class<?> objectType = ExampleObjectType.class;
		constructorDelegate = accessorTypeProvider.getConstructorType(ExampleConstructorDelegate.class, objectType);
		exampleIntAccessor = accessorTypeProvider.getAccessorType(objectType, propertyInfoProvider.getProperty(objectType, "ExampleIntValue"));
		exampleLongAccessor = accessorTypeProvider.getAccessorType(objectType, propertyInfoProvider.getProperty(objectType, "ExampleLongValue"));
	}

	public void exampleInstantiation() {
		Object obj1 = constructorDelegate.create(1);
		Object obj2 = constructorDelegate.create(2L);
		exampleIntAccessor.getValue(obj1);
		exampleLongAccessor.getValue(obj2);
	}
}
