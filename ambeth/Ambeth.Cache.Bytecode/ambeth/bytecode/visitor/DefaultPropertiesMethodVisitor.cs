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
                if (!propertyInfo.IsWritable || !propertyInfo.IsReadable)
                {
                    continue;
                }
                MethodInstance m_setterTemplate = new MethodInstance(((MethodPropertyInfo)propertyInfo).Setter);
                MethodInstance m_setter = MethodInstance.FindByTemplate(m_setterTemplate, true);

                FieldInstance f_backingField = null;
                if (m_setter == null)
                {
                    f_backingField = EnsureBackingField(propertyInfo);
                    if (f_backingField == null)
                    {
                        continue;
                    }
                    // implement setter
                    ImplementSetter(m_setterTemplate, f_backingField);
                }

                MethodInstance m_getterTemplate = new MethodInstance(((MethodPropertyInfo)propertyInfo).Getter);
                MethodInstance m_getter = MethodInstance.FindByTemplate(m_getterTemplate, true);

                if (m_getter == null)
                {
                    if (f_backingField == null)
                    {
                        f_backingField = EnsureBackingField(propertyInfo);
                    }
                    if (f_backingField == null)
                    {
                        continue;
                    }
                    // implement getter
                    ImplementGetter(m_getterTemplate, f_backingField);
                }
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