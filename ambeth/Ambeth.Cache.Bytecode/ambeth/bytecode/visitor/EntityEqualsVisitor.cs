using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityEqualsVisitor : ClassVisitor
    {
        private static readonly MethodInstance entityEquals_Equals = new MethodInstance(null, typeof(EntityEqualsVisitor),
                typeof(bool), "EntityEquals_Equals", typeof(IEntityEquals), typeof(Object));

        private static readonly MethodInstance entityEquals_HashCode = new MethodInstance(null, typeof(EntityEqualsVisitor),
                typeof(int), "EntityEquals_GetHashCode", typeof(IEntityEquals));

        private static readonly MethodInstance entityEquals_toString_Obj = new MethodInstance(null, typeof(EntityEqualsVisitor),
                typeof(String), "EntityEquals_ToString", typeof(IEntityEquals), typeof(IPrintable));

        private static readonly MethodInstance entityEquals_toString_Printable = new MethodInstance(null,
                typeof(EntityEqualsVisitor), typeof(void), "EntityEquals_ToString", typeof(IEntityEquals), typeof(StringBuilder));

        public EntityEqualsVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            ImplementIEntityEqualsCode();
            base.VisitEnd();
        }

        protected void ImplementIEntityEqualsCode()
        {
            ImplementEqualsMethod();
            ImplementHashCodeMethod();
            ImplementToStringMethod();
        }

        protected void ImplementEqualsMethod()
        {
            MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(bool), "Equals", typeof(Object));
            MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
            if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
            {
                IMethodVisitor mg = VisitMethod(methodTemplate);
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeStatic(entityEquals_Equals);
                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementHashCodeMethod()
        {
            MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(int), "GetHashCode");
            MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
            if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
            {
                IMethodVisitor mg = VisitMethod(methodTemplate);
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeStatic(entityEquals_HashCode);

                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementToStringMethod()
        {
            {
                MethodInstance methodTemplate = new MethodInstance(null, typeof(Object), typeof(String), "ToString");
                MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
                if (NewType.GetType(typeof(Object)).Equals(method.Owner) || method.Access.HasFlag(MethodAttributes.Abstract))
                {
                    IMethodVisitor mg = VisitMethod(methodTemplate);
                    mg.LoadThis();
                    mg.LoadThis();
                    mg.InvokeStatic(entityEquals_toString_Obj);
                    mg.ReturnValue();
                    mg.EndMethod();
                }
            }

            {
                MethodInstance methodTemplate = new MethodInstance(null, typeof(IPrintable), typeof(void), "ToString", typeof(StringBuilder));
                MethodInstance method = MethodInstance.FindByTemplate(methodTemplate, true);
                if (method == null || method.Access.HasFlag(MethodAttributes.Abstract))
                {
                    IMethodVisitor mg = VisitMethod(methodTemplate);
                    mg.LoadThis();
                    mg.LoadArgs();
                    mg.InvokeStatic(entityEquals_toString_Printable);
                    mg.ReturnValue();
                    mg.EndMethod();
                }
            }
        }

        public static bool EntityEquals_Equals(IEntityEquals left, Object right)
        {
            if (right == left)
            {
                return true;
            }
            if (!(right is IEntityEquals))
            {
                return false;
            }
            Object id = left.Get__Id();
            if (id == null)
            {
                // Null id can never be equal with something other than itself
                return false;
            }
            IEntityEquals other = (IEntityEquals)right;
            return id.Equals(other.Get__Id()) && left.Get__BaseType().Equals(other.Get__BaseType());
        }

        public static int EntityEquals_GetHashCode(IEntityEquals left)
        {
            Object id = left.Get__Id();
            if (id == null)
            {
                return RuntimeHelpers.GetHashCode(left);
            }
            return left.Get__BaseType().GetHashCode() ^ id.GetHashCode();
        }

        public static String EntityEquals_ToString(IEntityEquals left, IPrintable printable)
        {
            StringBuilder sb = new StringBuilder();
            printable.ToString(sb);
            return sb.ToString();
        }

        public static void EntityEquals_ToString(IEntityEquals left, StringBuilder sb)
        {
            sb.Append(left.Get__BaseType().FullName).Append('-');
            StringBuilderUtil.AppendPrintable(sb, left.Get__Id());
        }
    }
}