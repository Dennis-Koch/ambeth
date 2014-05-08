using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reflection;
using System.Windows.Input;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;
using De.Osthus.Ambeth.Log;
using System.Collections;

namespace De.Osthus.Minerva.Command
{
    public class ControllerServiceCommand : AbstractModelContainerRelatedCommand
    {
        [LogInstance]
		public new ILogger Log { private get; set; }

        public virtual Object ControllerService { get; set; }

        public virtual String MethodName { get; set; }

        public virtual Type[] MethodParameters { get; set; }

        public virtual MethodInfo Method { get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(ControllerService, "ControllerService");
            if (Method == null)
            {
                ParamChecker.AssertNotNull(MethodName, "MethodName");

                if (MethodParameters == null)
                {
                    Method = ControllerService.GetType().GetMethod(MethodName);
                }
                else
                {
                    Method = ControllerService.GetType().GetMethod(MethodName, MethodParameters);
                }
                if (Method == null)
                {
                    throw new ArgumentException("No method '" + MethodName + "' found on type '" + ControllerService.GetType().FullName);
                }
            }
        }

        public override void Execute(Object parameter)
        {
            IList<Object> businessObjects = ExtractBusinessObjects(parameter);

            if (businessObjects == null || businessObjects.Count == 0)
            {
                return;
            }
            Object[] methodArgumentArray = CreateMethodArgument(businessObjects, Method.GetParameters()[0].ParameterType);

            foreach (Object methodArgument in methodArgumentArray)
            {
                Method.Invoke(ControllerService, new Object[] { methodArgument });
            }
        }

        protected virtual Object[] CreateMethodArgument(IList<Object> businessObjects, Type expectedType)
        {
            int objectCount = businessObjects.Count;
            if (typeof(IEnumerable).IsAssignableFrom(expectedType))
            {
                Object targetCollection = ListUtil.CreateCollectionOfType(expectedType);

                MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
                Object[] parameters = new Object[1];

                for (int a = 0; a < objectCount; a++)
                {
                    parameters[0] = businessObjects[a];
                    addMethod.Invoke(targetCollection, parameters);
                }
                return new Object[] {targetCollection};
            }
            else if (expectedType.IsArray)
            {
                Array array = Array.CreateInstance(expectedType.GetElementType(), objectCount);
                for (int a = 0; a < objectCount; a++)
                {
                    array.SetValue(businessObjects[a], a);
                }
                return new Object[] {array};
            }
            return ListUtil.ToArray(businessObjects);
        }
    }
}
