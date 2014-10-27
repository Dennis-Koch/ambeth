using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
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
            HashMap<String, List<MethodInfo>> nameToMethodsMap = new HashMap<String, List<MethodInfo>>();
		    foreach (MethodInfo method in ReflectUtil.GetMethods(State.OriginalType))
		    {
			    List<MethodInfo> methodList = nameToMethodsMap.Get(method.Name);
			    if (methodList == null)
			    {
				    methodList = new List<MethodInfo>();
				    nameToMethodsMap.Put(method.Name, methodList);
			    }
			    methodList.Add(method);
		    }
		    foreach (IPropertyInfo propertyInfo in propertyInfos)
		    {
			    MethodInfo getter = ((MethodPropertyInfo) propertyInfo).Getter;
			    MethodInfo setter = ((MethodPropertyInfo) propertyInfo).Setter;
			    if (getter == null)
			    {
				    // look for abstract definition of the getter
				    getter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, propertyInfo.PropertyType, "get_" + propertyInfo.Name);
			    }
			    if (setter == null)
			    {
				    // look for abstract definition of the setter
				    setter = ReflectUtil.GetDeclaredMethod(true, State.CurrentType, typeof(void), "set_" + propertyInfo.Name,
						    propertyInfo.PropertyType);
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
                    m_setterTemplate = new MethodInstance(null, MethodAttributes.Public | MethodAttributes.SpecialName, m_setterTemplate != null ? m_setterTemplate.ReturnType : NewType.VOID_TYPE,
						    "set_" + propertyInfo.Name, f_backingField.Type);
			    }
			    // implement setter
			    m_setterTemplate = ImplementSetter(m_setterTemplate, f_backingField);
			    List<MethodInfo> allSettersWithSameName = nameToMethodsMap.Get(m_setterTemplate.Name);
			    if (allSettersWithSameName != null)
			    {
				    MethodInstance f_m_setterTemplate = m_setterTemplate;
				    foreach (MethodInfo setterWithSameName in allSettersWithSameName)
				    {
					    MethodInstance m_setterWithSameName = MethodInstance.FindByTemplate(setterWithSameName, true);
					    if (m_setterWithSameName != null)
					    {
						    // method is implemented, so nothing to do
						    continue;
					    }
					    IMethodVisitor mv = VisitMethod(new MethodInstance(setterWithSameName));
					    if (mv.Method.Parameters.Length != 1)
					    {
						    // this visitor handles only "true" setters with exactly one argument
						    continue;
					    }
					    mv.CallThisSetter(m_setterTemplate, delegate(IMethodVisitor mg)
						    {
							    mg.LoadArg(0);
							    mg.CheckCast(f_m_setterTemplate.Parameters[0].Type);
						    });
					    mv.ReturnVoidOrThis();
					    mv.EndMethod();
				    }
			    }
			    if (m_getterTemplate == null)
			    {
				    m_getterTemplate = new MethodInstance(null, MethodAttributes.Public | MethodAttributes.SpecialName, f_backingField.Type, "get_" + propertyInfo.Name, null);
			    }
			    // implement getter
			    m_getterTemplate = ImplementGetter(m_getterTemplate, f_backingField);
			    List<MethodInfo> allGettersWithSameName = nameToMethodsMap.Get(m_getterTemplate.Name);
			    if (allGettersWithSameName != null)
			    {
				    foreach (MethodInfo getterWithSameName in allGettersWithSameName)
				    {
					    MethodInstance m_getterWithSameName = MethodInstance.FindByTemplate(getterWithSameName, true);
					    if (m_getterWithSameName != null)
					    {
						    // method is implemented, so nothing to do
						    continue;
					    }
					    IMethodVisitor mv = VisitMethod(new MethodInstance(getterWithSameName));
					    mv.CallThisGetter(m_getterTemplate);
					    mv.ReturnValue();
					    mv.EndMethod();
				    }
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

                    f_backingField = ImplementField(f_backingField);
                }
                return f_backingField;
            }
            return null;
        }
    }
}