using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Typeinfo;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Model;
using System.Threading;

namespace De.Osthus.Ambeth.Merge.Interceptor
{
    public class MergeInterceptor : AbstractInterceptor<IMergeService, IMergeClient>, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected static readonly ThreadLocal<bool> processServiceActiveTL = new ThreadLocal<bool>(delegate() { return false; });

        public IConversionHelper ConversionHelper { protected get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IMergeProcess MergeProcess { protected get; set; }

        public IProcessService ProcessService { protected get; set; }

        public String ServiceName { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(MergeProcess, "MergeProcess");
        }

        protected override Object InterceptMergeIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin)
        {
            if (arguments == null || (arguments.Length != 1 && arguments.Length != 2 && arguments.Length != 3))
            {
                throw new Exception("Arguments currently must be only 1, 2 or 3: " + method.ToString());
            }

            Object argumentToMerge = arguments[0];
            Object argumentToDelete = GetArgumentToDelete(arguments, method.GetParameters());
            ProceedWithMergeHook proceedHook = GetProceedHook(arguments);
            MergeFinishedCallback finishedCallback = GetFinishedCallback(arguments);
            MergeProcess.Process(argumentToMerge, argumentToDelete, proceedHook, finishedCallback);
            if (!typeof(void).Equals(method.ReturnType))
            {
                return argumentToMerge;
            }
            return null;
        }

        protected override Object InterceptDeleteIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin)
        {
            if (arguments == null || (arguments.Length != 1 && arguments.Length != 3))
            {
                throw new Exception("Arguments currently must be only 1 or 3: " + method.ToString());
            }
            ProceedWithMergeHook proceedHook = GetProceedHook(arguments);
            MergeFinishedCallback finishedCallback = GetFinishedCallback(arguments);
            RemoveAttribute remove = AnnotationUtil.GetAnnotation<RemoveAttribute>(method, false);
            if (remove != null)
            {
                String idName = remove.IdName;
                Type entityType = remove.EntityType;
                if (idName != null && idName.Length > 0)
                {
                    if (entityType == null)
                    {
                        throw new Exception("Annotation invalid: " + remove + " on method " + method.ToString());
                    }
                    DeleteById(method, entityType, idName, arguments[0], proceedHook, finishedCallback);
                    return null;
                }
            }
            Object argumentToDelete = arguments[0];
            MergeProcess.Process(null, argumentToDelete, proceedHook, finishedCallback);
            if (!typeof(void).Equals(method.ReturnType))
            {
                return argumentToDelete;
            }
            return null;
        }

        protected override Object InterceptApplication(IInvocation invocation, Boolean? isAsyncBegin)
        {
            bool oldProcessServiceActive = processServiceActiveTL.Value;
            if (oldProcessServiceActive || ProcessService == null)
            {
                return base.InterceptApplication(invocation, isAsyncBegin);
            }
            IServiceDescription serviceDescription = SyncToAsyncUtil.CreateServiceDescription(ServiceName, invocation.Method, invocation.Arguments);
            processServiceActiveTL.Value = true;
            try
            {
                return ProcessService.InvokeService(serviceDescription);
            }
            finally
            {
                processServiceActiveTL.Value = oldProcessServiceActive;
            }
        }

        protected void DeleteById(MethodInfo method, Type entityType, String idName, Object ids, ProceedWithMergeHook proceedHook, MergeFinishedCallback finishedCallback)
        {
            IEntityMetaData metaData = GetSpecifiedMetaData(method, typeof(RemoveAttribute), entityType);
            ITypeInfoItem idMember = GetSpecifiedMember(method, typeof(RemoveAttribute), metaData, idName);
            sbyte idIndex = metaData.GetIdIndexByMemberName(idName);

            Type idType = idMember.RealType;
            List<IObjRef> objRefs = new List<IObjRef>();
            BuildObjRefs(entityType, idIndex, idType, ids, objRefs);
            MergeProcess.Process(null, objRefs, proceedHook, finishedCallback);
        }

        protected void BuildObjRefs(Type entityType, sbyte idIndex, Type idType, Object ids, IList<IObjRef> objRefs)
        {
            if (ids == null)
            {
                return;
            }
            if (ids is IList)
            {
                IList list = (IList)ids;
                for (int a = 0, size = list.Count; a < size; a++)
                {
                    Object id = list[a];
                    BuildObjRefs(entityType, idIndex, idType, id, objRefs);
                }
                return;
            }
            else if (ids is ICollection)
            {
                IEnumerator iter = ((ICollection)ids).GetEnumerator();
                while (iter.MoveNext())
                {
                    Object id = iter.Current;
                    BuildObjRefs(entityType, idIndex, idType, id, objRefs);
                }
                return;
            }
            else if (ids.GetType().IsArray)
            {
                Array array = (Array)ids;
                int size = array.Length;
                for (int a = 0; a < size; a++)
                {
                    Object id = array.GetValue(a);
                    BuildObjRefs(entityType, idIndex, idType, id, objRefs);
                }
                return;
            }
            Object convertedId = ConversionHelper.ConvertValueToType(idType, ids);
            ObjRef objRef = new ObjRef(entityType, idIndex, convertedId, null);
            objRefs.Add(objRef);
        }

        protected IEntityMetaData GetSpecifiedMetaData(MethodInfo method, Type annotation, Type entityType)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            if (metaData == null)
            {
                throw new Exception("Please specify a valid returnType for the " + annotation.Name + " annotation on method "
                        + method.ToString() + ". The current value " + entityType.FullName + " is not a valid entity");
            }
            return metaData;
        }

        protected ITypeInfoItem GetSpecifiedMember(MethodInfo method, Type annotation, IEntityMetaData metaData, String memberName)
        {
            if (memberName == null || memberName.Length == 0)
            {
                return metaData.IdMember;
            }
            ITypeInfoItem member = metaData.GetMemberByName(memberName);
            if (member == null)
            {
                throw new Exception("No member " + metaData.EntityType.FullName + "." + memberName + " found. Please check your "
                        + annotation.Name + " annotation on method " + method.ToString());
            }
            return member;
        }

        ///// <summary>
        ///// Filter method parameters that should not be serialized
        ///// </summary>
        ///// <param name="methodDescription">The method description to filter</param>
        //protected virtual void FilterParameters(MethodDescription methodDescription)
        //{
        //    List<Type> paramTypes = new List<Type>();
        //    foreach(Type type in methodDescription.ParamTypes) {
        //        if (!type.IsAssignableFrom(typeof(ProceedWithMergeHook))) {
        //            paramTypes.Add(type);
        //        }
        //    }
        //    methodDescription.ParamTypes = paramTypes.ToArray();
        //}

        protected virtual Object GetArgumentToDelete(Object[] args, ParameterInfo[] parameters)
        {
            if (parameters == null || parameters.Length < 2)
            {
                return null;
            }
            ParameterInfo parameterToLook = parameters[1];
            if (typeof(ProceedWithMergeHook).IsAssignableFrom(parameterToLook.ParameterType)
                || typeof(MergeFinishedCallback).IsAssignableFrom(parameterToLook.ParameterType))
            {
                return null;
            }
            return args[1];
        }

        protected virtual ProceedWithMergeHook GetProceedHook(Object[] args)
        {
            if (args == null)
            {
                return null;
            }
            for (int a = args.Length; a-- > 0; )
            {
                Object arg = args[a];
                if (arg is ProceedWithMergeHook)
                {
                    return (ProceedWithMergeHook)arg;
                }
            }
            return null;
        }

        protected virtual MergeFinishedCallback GetFinishedCallback(Object[] args)
        {
            if (args == null)
            {
                return null;
            }
            for (int a = args.Length; a-- > 0; )
            {
                Object arg = args[a];
                if (arg is MergeFinishedCallback)
                {
                    return (MergeFinishedCallback)arg;
                }
            }
            return null;
        }
    }
}