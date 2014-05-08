package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class SetCacheModificationMethodCreator extends ClassGenerator
{
	private static final MethodInstance callCacheModification = new MethodInstance(null, SetCacheModificationMethodCreator.class, "callCacheModification",
			ICacheModification.class, boolean.class, boolean.class);

	private static final String cacheModificationName = "$cacheModification";

	public static PropertyInstance getCacheModificationPI(ClassGenerator cv)
	{
		PropertyInstance pi = getState().getProperty(cacheModificationName);
		if (pi != null)
		{
			return pi;
		}
		Object bean = getState().getBeanContext().getService(ICacheModification.class);
		return cv.implementAssignedReadonlyProperty(cacheModificationName, bean);
	}

	public static void cacheModificationActive(PropertyInstance p_cacheModification, MethodGenerator mg, Script script)
	{
		final int loc_cacheModification = mg.newLocal(ICacheModification.class);
		final int loc_oldActive = mg.newLocal(boolean.class);

		// ICacheModification cacheModification = this.cacheModification;
		mg.callThisGetter(p_cacheModification);
		mg.storeLocal(loc_cacheModification);

		// boolean oldActive = cacheModification.isActive();
		mg.loadLocal(loc_cacheModification);
		mg.invokeInterface(new MethodInstance(null, ICacheModification.class, "isActive"));
		mg.storeLocal(loc_oldActive);

		// callModificationActive(cacheModification, oldActive, true)
		mg.loadLocal(loc_cacheModification);
		mg.loadLocal(loc_oldActive);
		mg.push(true);
		mg.invokeStatic(callCacheModification);

		mg.tryFinally(script, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				// callModificationActive(cacheModification, oldActive, false)
				mg.loadLocal(loc_cacheModification);
				mg.loadLocal(loc_oldActive);
				mg.push(false);
				mg.invokeStatic(callCacheModification);
			}
		});
	}

	public SetCacheModificationMethodCreator(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		// force implementation
		getCacheModificationPI(this);

		super.visitEnd();
	}

	public static void callCacheModification(ICacheModification cacheModification, boolean oldValue, boolean newValue)
	{
		if (!oldValue)
		{
			cacheModification.setActive(newValue);
		}
	}
}
