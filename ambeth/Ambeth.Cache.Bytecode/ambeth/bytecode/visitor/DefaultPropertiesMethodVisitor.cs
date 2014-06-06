using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class DefaultPropertiesMethodVisitor : ClassVisitor
    {
        protected IPropertyInfo[] propertyInfos;

        public DefaultPropertiesMethodVisitor(IClassVisitor cv, IPropertyInfo[] propertyInfos)
            : base(cv)
        {
            this.propertyInfos = propertyInfos;
        }

        public override void VisitEnd()
        {
            foreach (IPropertyInfo propertyInfo in propertyInfos)
            {
                MethodInfo getter = ((MethodPropertyInfo)propertyInfo).Getter;
                MethodInfo setter = ((MethodPropertyInfo)propertyInfo).Setter;

                if (getter == null)
                {
                    // look for abstract definition of the getter
                    getter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, "get_" + propertyInfo.Name);
                    if (getter == null)
                    {
                        getter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, "Get" + propertyInfo.Name);
                    }
                }
                if (setter == null)
                {
                    // look for abstract definition of the setter
                    setter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, "set_" + propertyInfo.Name,
                            propertyInfo.PropertyType);
                    if (setter == null)
                    {
                        setter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, "Set" + propertyInfo.Name,
                                propertyInfo.PropertyType);
                    }
                }
                MethodInstance m_getterTemplate = getter != null ? new MethodInstance(getter) : null;
                MethodInstance m_setterTemplate = setter != null ? new MethodInstance(setter) : null;
                MethodInstance m_getter = MethodInstance.FindByTemplate(m_getterTemplate, true);
                MethodInstance m_setter = MethodInstance.FindByTemplate(m_setterTemplate, true);

                if (m_getter != null || m_setter != null)
                {
                    // at least one of the accessors is explicitly implemented
                    continue;
                }
                FieldInstance f_backingField = EnsureBackingField(propertyInfo);
                if (f_backingField == null)
                {
                    continue;
                }
                if (m_setterTemplate == null)
                {
                    m_setterTemplate = new MethodInstance(null, MethodAttributes.Public, "set_" + propertyInfo.Name, null, m_setterTemplate != null ? m_setterTemplate.ReturnType : NewType.VOID_TYPE,
                            f_backingField.Type);
                }
                // implement setter
                ImplementSetter(m_setterTemplate, f_backingField);

                if (m_getterTemplate == null)
                {
                    m_getterTemplate = new MethodInstance(null, MethodAttributes.Public, "get_" + propertyInfo.Name, null, f_backingField.Type);
                }
                // implement getter
                ImplementGetter(m_getterTemplate, f_backingField);
            }
            base.VisitEnd();
        }

        protected FieldInstance EnsureBackingField(IPropertyInfo propertyInfo)
        {
            FieldInfo backingField = propertyInfo.BackingField;
            FieldInstance f_backingField;
            if (backingField != null)
            {
                return new FieldInstance(backingField);
            }
            else if (propertyInfo.DeclaringType.IsInterface || propertyInfo.DeclaringType.Attributes.HasFlag(TypeAttributes.Abstract))
            {
                String fieldName = StringConversionHelper.LowerCaseFirst(propertyInfo.Name);
                f_backingField = State.GetAlreadyImplementedField(fieldName);

                if (f_backingField == null)
                {
                    // add field
                    f_backingField = new FieldInstance(FieldAttributes.Family, StringConversionHelper.LowerCaseFirst(propertyInfo.Name),
                            NewType.GetType(propertyInfo.PropertyType));

                    ImplementField(f_backingField);
                }
                return f_backingField;
            }
            return null;
        }
    }
}