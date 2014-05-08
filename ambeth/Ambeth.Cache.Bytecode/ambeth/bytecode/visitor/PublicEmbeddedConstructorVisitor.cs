using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class PublicEmbeddedConstructorVisitor : ClassVisitor
    {
        public static readonly String PARENT_FIELD_NAME = "parent";

        public PublicEmbeddedConstructorVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            FieldInstance f_parent = new FieldInstance(FieldAttributes.Family | FieldAttributes.InitOnly, "parent", typeof(Object));
            ImplementField(f_parent);

            Type superType = State.CurrentType;
            ConstructorInfo[] superConstructors = superType.GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance);
            if (superConstructors.Length == 0)
            {
                // Default constructor
                ConstructorInstance superConstructor = new ConstructorInstance(typeof(Object).GetConstructor(null));
                ImplementConstructor(f_parent, superConstructor);
            }
            else
            {
                foreach (ConstructorInfo rSuperConstructor in superConstructors)
                {
                    ConstructorInstance superConstructor = new ConstructorInstance(rSuperConstructor);
                    ImplementConstructor(f_parent, superConstructor);
                }
            }
            base.VisitEnd();
        }

        protected void ImplementConstructor(FieldInstance f_parent, ConstructorInstance superConstructor)
        {
            NewType[] argTypes = superConstructor.Parameters;
            NewType[] newArgTypes = new NewType[argTypes.Length + 1];
            Array.Copy(argTypes, 0, newArgTypes, 0, argTypes.Length);
            newArgTypes[argTypes.Length] = NewType.GetType(typeof(Object));

            MethodAttributes access = superConstructor.Access;
            // Turn off private and protected
            access &= ~MethodAttributes.Private;
            access &= ~MethodAttributes.Family;
            // Turn on public
            access |= MethodAttributes.Public;

            // TODO: build new signature according to the newArgTypes

            ConstructorInstance constructor = new ConstructorInstance(access, newArgTypes);

            IMethodVisitor mv = VisitMethod(constructor);
            mv.LoadThis();
            mv.InvokeConstructor(superConstructor);
            mv.PutThisField(f_parent, delegate(IMethodVisitor mv2)
                {
                    mv2.LoadArg(0);
                });
            mv.ReturnValue();
            mv.EndMethod();
        }
    }
}