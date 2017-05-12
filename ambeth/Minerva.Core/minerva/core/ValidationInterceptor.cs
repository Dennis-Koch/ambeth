using System;
using System.Linq;
using System.ComponentModel.DataAnnotations;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using System.Reflection;
using System.ComponentModel;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Minerva.Core.Config;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Log;
using System.Text;

namespace De.Osthus.Minerva.Core
{
    /// <summary>
    /// IDataErrorInfo uses the index operator to provide information about suceeded validation of an object.
    /// This Interceptor adds a index operator to BOs and calls a method for validation via DataAnnotations first.
    /// Moreover it delegates the call to the BO itself.
    /// 
    /// TODO: Async validation. The interceptor could open up a new thread here and call the setter of an BO in a callback.
    /// Another solution would be to implement a special CustomValidationDataAnnotation which does this.
    /// The advantage of the first method is: we can skip the interception-logic of the setter (we don't really want to change a value and we don't want sideeffects except the tooltip on the gui).
    /// </summary>
    public class ValidationInterceptor : IInterceptor, IDataErrorInfo
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        //Intercept bo["PropertyName"]
        public void Intercept(IInvocation invocation)
        {
            if (!"get_Item".Equals(invocation.Method.Name))
            {
                invocation.Proceed();
                return;
            }
            Object proxy = invocation.Proxy;
            if (proxy == null)
            {
                return;
            }
            Type proxyType = proxy.GetType();
            String propertyName = (String)invocation.Arguments[0];
            if (propertyName == null)
            {
                return;
            }
            PropertyInfo property = proxyType.GetProperty(propertyName);
            if (property == null)
            {
                String msg1 = "Property: \"" + proxyType.Name + "." + propertyName + "\" could not be found.";
                if (Log.DebugEnabled)
                {
                    Log.Debug(msg1);
                }
                //invocation.ReturnValue = msg1;
                return;
            }
            Object value = property.GetValue(proxy, null);
            //Start the real logic
            //if (value == null)
            //{
            //    value = "(null)";
            //}
            //String msg = "Property: \"" + proxyType.Name + "." + propertyName +"\" is: \"" + value +"\".";
            //if (Log.DebugEnabled)
            //{
            //    Log.Debug(msg);
            //}
            ICollection<ValidationResult> validationResults = new List<ValidationResult>();
            //TODO: Build Validation Context... Or simply get the baseclass of the proxyobject, so we can get annotations. How is it done for ParentChild-Attribute?
            //IServiceProvider serviceProvider = null;//new BeanContextServiceProvider(null);
            //Sorry, attributes are not inheriated.
            //Validator.TryValidateProperty(value, new ValidationContext(proxy, serviceProvider, null) { MemberName = propertyName, ObjectType = property.DeclaringType }, validationResults);
            MemberInfo mi = proxyType.GetMember(propertyName)[0];
            Validator.TryValidateValue(value, new ValidationContext(proxy, null, null) { }, validationResults, property.GetCustomAttributes(true).OfType<ValidationAttribute>());
            //TODO: Thios is an interfaceinterceptor, target unknown, so no attibutes will be avaliable here. Put htis code int ClientEntityInterceptor
            StringBuilder sb = new StringBuilder();
            foreach (ValidationResult validationResult in validationResults)
            {
                sb.AppendLine(validationResult.ErrorMessage);
            }
            //if (typeof(IDataErrorInfo).IsAssignableFrom(proxyType))
            //{
            //    invocation.Proceed();
            //    sb.Insert(0, (String)invocation.ReturnValue);
            //}
            //Empty String indicates: There was no error. (No Tooltip will be shown in Gui)
            invocation.ReturnValue = sb.ToString();
        }

        public string Error
        {
            get { throw new NotImplementedException(); }
        }

        public string this[string columnName]
        {
            get { throw new NotImplementedException(); }
        }
    }

    internal class BeanContextServiceProvider : IServiceProvider
    {
        protected IServiceContext ServiceContext { get; set; }

        BeanContextServiceProvider(IServiceContext bc)
        {
            ServiceContext = bc;
        }
        public object GetService(Type serviceType)
        {
            return ServiceContext.GetService(serviceType);
        }
    }
}
