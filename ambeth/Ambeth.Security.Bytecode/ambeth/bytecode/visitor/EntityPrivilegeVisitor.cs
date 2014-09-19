using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityPrivilegeVisitor : ClassVisitor
    {
        protected readonly IEntityMetaData metaData;
        protected readonly bool create;
        protected readonly bool read;
        protected readonly bool update;
        protected readonly bool delete;
        protected readonly bool execute;

        public EntityPrivilegeVisitor(IClassVisitor cv, IEntityMetaData metaData, bool create, bool read, bool update, bool delete, bool execute) : 
            base(cv)
        {
            this.metaData = metaData;
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
        }


        public void visitEnd()
        {
            // AbstractPrivilege.class;
            MethodInstance template_m_getPrimitivePropertyPrivilege = new MethodInstance(null, typeof(IPrivilege), typeof(IPropertyPrivilege),
                    "GetPrimitivePropertyPrivilege", typeof(int));

            MethodInstance template_m_setPrimitivePropertyPrivilege = new MethodInstance(null, MethodAttributes.Family, typeof(void), "SetPrimitivePropertyPrivilege",
                    typeof(int), typeof(IPropertyPrivilege));

            MethodInstance template_m_getRelationPropertyPrivilege = new MethodInstance(null, typeof(IPrivilege), typeof(IPropertyPrivilege),
                    "GetRelationPropertyPrivilege", typeof(int));

            MethodInstance template_m_setRelationPropertyPrivilege = new MethodInstance(null, MethodAttributes.Family, typeof(void), "SetRelationPropertyPrivilege",
                    typeof(int), typeof(IPropertyPrivilege));

            ImplementGetSetPropertyPrivilege(metaData.PrimitiveMembers, template_m_getPrimitivePropertyPrivilege, template_m_setPrimitivePropertyPrivilege);
            ImplementGetSetPropertyPrivilege(metaData.RelationMembers, template_m_getRelationPropertyPrivilege, template_m_setRelationPropertyPrivilege);

            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(bool), "get_CreateAllowed"));
                mg.Push(create);
                mg.ReturnValue();
                mg.EndMethod();
            }
            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(bool), "get_ReadAllowed"));
                mg.Push(read);
                mg.ReturnValue();
                mg.EndMethod();
            }
            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(bool), "get_UpdateAllowed"));
                mg.Push(update);
                mg.ReturnValue();
                mg.EndMethod();
            }
            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(bool), "get_DeleteAllowed"));
                mg.Push(delete);
                mg.ReturnValue();
                mg.EndMethod();
            }
            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(bool), "get_ExecuteAllowed"));
                mg.Push(execute);
                mg.ReturnValue();
                mg.EndMethod();
            }
            {
                IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrivilege), typeof(IPropertyPrivilege), "GetDefaultPropertyPrivilegeIfValid"));
                mg.PushNull();
                mg.ReturnValue();
                mg.EndMethod();
            }
            ImplementConstructor(template_m_setPrimitivePropertyPrivilege, template_m_setRelationPropertyPrivilege);
            base.VisitEnd();
        }

        protected void ImplementConstructor(MethodInstance template_m_setPrimitivePropertyPrivilege, MethodInstance template_m_setRelationPropertyPrivilege)
        {
            IBytecodeBehaviorState state = State;
            ConstructorInfo constructor = state.CurrentType.GetConstructor(new Type[] {typeof(bool), typeof(bool), typeof(bool), typeof(bool), typeof(bool),
					typeof(IPropertyPrivilege[]), typeof(IPropertyPrivilege[])});
            ConstructorInstance c_method = new ConstructorInstance(constructor);

            IMethodVisitor mg = VisitMethod(c_method);
            mg.LoadThis();
            mg.LoadArgs();
            mg.InvokeConstructor(c_method);

            Type propertyPrivilegeType = typeof(IPropertyPrivilege);
            for (int primitiveIndex = 0, size = metaData.PrimitiveMembers.Length; primitiveIndex < size; primitiveIndex++)
            {
                mg.LoadThis();
                mg.Push(primitiveIndex);
                mg.LoadArg(5);
                mg.Push(primitiveIndex);
                mg.ArrayLoad(propertyPrivilegeType);
                mg.InvokeVirtual(template_m_setPrimitivePropertyPrivilege);
            }
            for (int relationIndex = 0, size = metaData.RelationMembers.Length; relationIndex < size; relationIndex++)
            {
                mg.LoadThis();
                mg.Push(relationIndex);
                mg.LoadArg(6);
                mg.Push(relationIndex);
                mg.ArrayLoad(propertyPrivilegeType);
                mg.InvokeVirtual(template_m_setRelationPropertyPrivilege);
            }

            mg.ReturnValue();
            mg.EndMethod();
        }

        protected void ImplementGetSetPropertyPrivilege(Member[] members, MethodInstance template_getPropertyPrivilege, MethodInstance template_setPropertyPrivilege)
        {
            FieldInstance[] fields = new FieldInstance[members.Length];

            for (int index = 0, size = members.Length; index < size; index++)
            {
                Member member = members[index];
                FieldInstance field = ImplementField(new FieldInstance(FieldAttributes.Private, GetFieldName(member), typeof(IPropertyPrivilege)));
                fields[index] = field;
            }
            ImplementGetPropertyPrivilege(fields, template_getPropertyPrivilege);
            ImplementSetPropertyPrivilege(fields, template_setPropertyPrivilege);
        }

        protected void ImplementGetPropertyPrivilege(FieldInstance[] fields, MethodInstance template_getPropertyPrivilege)
        {
            ImplementSwitchByIndex(template_getPropertyPrivilege, "Given memberIndex not known", fields.Length, delegate(IMethodVisitor mg, int fieldIndex)
                {
                    mg.GetThisField(fields[fieldIndex]);
                    mg.ReturnValue();
                });
        }

        protected void ImplementSetPropertyPrivilege(FieldInstance[] fields, MethodInstance template_setPropertyPrivilege)
        {
            ImplementSwitchByIndex(template_setPropertyPrivilege, "Given memberIndex not known", fields.Length, delegate(IMethodVisitor mg, int fieldIndex)
                {
                    mg.PutThisField(fields[fieldIndex], delegate(IMethodVisitor mg2)
                        {
                            mg2.LoadArg(1);
                        });
                    mg.ReturnValue();
                });
        }

        public static String GetFieldName(Member member)
        {
            return member.Name.Replace('.', '_');
        }
    }
}