using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Reflection;
using De.Osthus.Ambeth.Ioc.Link;

namespace De.Osthus.Minerva.Bind
{
    public class EventsUtil
    {
        public static readonly Type EventDelegateType = typeof(EventDelegate<Object>).GetGenericTypeDefinition();
        
        public static void initFields(Type type, SetFieldDelegate setFieldDelegate)
        {
            //FieldInfo[] fields = type.GetFields(BindingFlags.Static | BindingFlags.Public | BindingFlags.DeclaredOnly);

            //for (int a = fields.Length; a-- > 0;)
            //{
            //    FieldInfo field = fields[a];

            //    Type fieldType = field.FieldType;

            //    if (!fieldType.IsGenericType)
            //    {
            //        continue;
            //    }
            //    Type[] genericArgTypes = fieldType.GetGenericArguments();
            //    if (genericArgTypes == null || genericArgTypes.Length != 1)
            //    {
            //        continue;
            //    }
            //    Object eventDelegate = Activator.CreateInstance(EventDelegateType.MakeGenericType(genericArgTypes), field.Name);
            //    setFieldDelegate(field, eventDelegate);
            //}

            PropertyInfo[] properties = type.GetProperties(BindingFlags.Static | BindingFlags.Public | BindingFlags.DeclaredOnly);

            for (int a = properties.Length; a-- > 0; )
            {
                PropertyInfo property = properties[a];

                Type propertyType = property.PropertyType;

                if (!propertyType.IsGenericType || !property.CanWrite)
                {
                    continue;
                }
                Type[] genericArgTypes = propertyType.GetGenericArguments();
                if (genericArgTypes == null || genericArgTypes.Length != 1)
                {
                    continue;
                }
                if (property.CanRead)
                {
                     Object existingValue = property.GetValue(null, null);
                     if (existingValue != null)
                     {
                         // This property already has a valid value so we are skipping it here
                         continue;
                     }
                }
                //"<GotFocus>k__BackingField"
                String fieldName = "<" + property.Name + ">k__BackingField";

                FieldInfo field = type.GetField(fieldName, BindingFlags.Static | BindingFlags.NonPublic);
                Object eventDelegate = Activator.CreateInstance(EventDelegateType.MakeGenericType(genericArgTypes), property.Name);
                setFieldDelegate.Invoke(field, eventDelegate);
            }
        }
    }

    public delegate void SetFieldDelegate(FieldInfo field, Object eventDelegate);
}
