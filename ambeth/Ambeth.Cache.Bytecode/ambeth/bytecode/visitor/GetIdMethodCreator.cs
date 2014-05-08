using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class GetIdMethodCreator : ClassVisitor
    {
        protected readonly IEntityMetaData metaData;

        private static readonly MethodInstance template_m_entityEquals_getId = new MethodInstance(null, typeof(IEntityEquals), "Get__Id");

        public static MethodInstance GetGetId()
        {
            return MethodInstance.FindByTemplate(template_m_entityEquals_getId, false);
        }

        public GetIdMethodCreator(IClassVisitor cv, IEntityMetaData metaData)
            : base(cv)
        {
            this.metaData = metaData;
        }

        public override void VisitEnd()
        {
            MethodInstance m_get__Id = MethodInstance.FindByTemplate(GetIdMethodCreator.template_m_entityEquals_getId, true);
            if (m_get__Id != null)
            {
                base.VisitEnd();
                return;
            }
            m_get__Id = GetIdMethodCreator.template_m_entityEquals_getId.DeriveOwner();

            IMethodVisitor mg = VisitMethod(m_get__Id);

            ITypeInfoItem idMember = metaData.IdMember;

            Type idType = idMember.RealType;
            mg.InvokeGetValue(idMember, delegate(IMethodVisitor mg2)
                {
                    mg2.LoadThis();
                });
            if (idType.IsPrimitive)
            {
                LocalVariableInfo localValue = mg.NewLocal(idType);
                mg.StoreLocal(localValue);
                mg.LoadLocal(localValue);
                Label l_idNotNull = mg.NewLabel();
                mg.IfZCmp(CompareOperator.NE, l_idNotNull);
                mg.PushNull();
                mg.ReturnValue();
                mg.Mark(l_idNotNull);
                mg.LoadLocal(localValue);
                mg.Box(idType);
            }
            else if (idType.FullName.StartsWith("System.Nullable`"))
            {
                Label l_idIsNullOrZero = mg.NewLabel();
                Type genericArgType = idType.GetGenericArguments()[0];
                LocalVariableInfo localStructValue = mg.NewLocal(idType);
                LocalVariableInfo localValue = mg.NewLocal(genericArgType);

                mg.StoreLocal(localStructValue);
                
                mg.LoadLocal(localStructValue);
                mg.InvokeOnExactOwner(idType.GetMethod("get_HasValue"));
                mg.IfZCmp(CompareOperator.EQ, l_idIsNullOrZero);

                mg.LoadLocal(localStructValue);
                mg.InvokeOnExactOwner(idType.GetMethod("get_Value"));
                mg.StoreLocal(localValue);

                mg.LoadLocal(localValue);
                mg.IfZCmp(CompareOperator.EQ, l_idIsNullOrZero);

                mg.LoadLocal(localValue);
                mg.Box(genericArgType);
                mg.ReturnValue();

                mg.Mark(l_idIsNullOrZero);
                mg.PushNull();
            }
            else if (idType.IsValueType)
            {
                mg.Box(idType);
            }
            mg.ReturnValue();
            mg.EndMethod();

            base.VisitEnd();
        }

        protected Type GetDeclaringType(MemberInfo member, Type newEntityType)
        {
            if (member.DeclaringType.IsInterface)
            {
                return newEntityType;
            }
            return member.DeclaringType;
        }
    }
}