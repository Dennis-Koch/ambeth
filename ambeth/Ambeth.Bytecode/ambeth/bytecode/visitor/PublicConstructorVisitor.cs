using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    /**
     * PublicConstructorVisitor declares constructors derived from {@link #superClass}.
     * 
     * {@link #superClass} is defined either by {@link BytecodeBehaviorState#getCurrentType()} or by the parameter extendedType of the
     * {@link PublicConstructorVisitor#PublicConstructorVisitor(ClassVisitor, Class)} constructor. In the latter case extendedType can be an abstract type used as
     * template to implement the interface defined by {@link BytecodeBehaviorState#getOriginalType()}.
     * 
     * If {@link #superClass} does not declare constructors or {@link #superClass} is an interface a default constructor is created.
     */
    public class PublicConstructorVisitor : ClassVisitor
    {
        public static bool HasValidConstructor()
	    {
		    IBytecodeBehaviorState state = BytecodeBehaviorState.State;

            ConstructorInfo[] constructors = state.CurrentType.GetConstructors(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly);

		    foreach (ConstructorInfo constructor in constructors)
		    {
			    if (state.IsMethodAlreadyImplementedOnNewType(new ConstructorInstance(constructor)))
			    {
				    return true;
			    }
		    }
		    return false;
	    }

        /**
         * Derives constructors from {@link BytecodeBehaviorState#getState()#getCurrentType()}
         * 
         * @param cv
         *            ClassVisitor
         */
        public PublicConstructorVisitor(IClassVisitor cv) : base(cv)
        {
            // Intended blank
        }

        /**
         * {@inheritDoc}
         */
        public override void VisitEnd()
        {
            if (!HasValidConstructor())
            {
                IBytecodeBehaviorState state = State;
                ConstructorInfo[] constructors = state.CurrentType.GetConstructors(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly);

                foreach (ConstructorInfo constructor in constructors)
                {
                    MethodAttributes access = constructor.Attributes;
                    access &= ~MethodAttributes.Family;
                    access &= ~MethodAttributes.Private;
                    access |= MethodAttributes.Public;
                    ConstructorInstance c_method = new ConstructorInstance(access, TypeUtil.GetClassesToTypes(constructor.GetParameters()));

                    IMethodVisitor mg = VisitMethod(c_method);
                    mg.LoadThis();
                    mg.LoadArgs();
                    mg.InvokeConstructor(new ConstructorInstance(constructor));

                    mg.ReturnValue();
                    mg.EndMethod();
                }
                if (constructors.Length == 0)
                {
                    // Implement "first" default constructor
                    ConstructorInstance c_method = new ConstructorInstance(typeof(Object).GetConstructor(null));
                    IMethodVisitor ga = VisitMethod(c_method);
                    ga.LoadThis();
                    ga.InvokeConstructor(c_method);
                    ga.ReturnValue();
                    ga.EndMethod();
                }
            }
            base.VisitEnd();
        }
    }
}