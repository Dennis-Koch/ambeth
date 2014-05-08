using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Pending;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public class MergeXmlPostProcessor : IXmlPostProcessor, IInitializingBean, IStartingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IServiceContext BeanContext { protected get; set; }

        public ICacheFactory CacheFactory { protected get; set; }

        public ICommandBuilder CommandBuilder { protected get; set; }

        public IMergeController MergeController { protected get; set; }

        public IObjRefHelper OriHelper { protected get; set; }

        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        protected ITypeInfoItem DirectObjRefDirectMember { get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "beanContext");
            ParamChecker.AssertNotNull(CacheFactory, "cacheFactory");
            ParamChecker.AssertNotNull(CommandBuilder, "commandBuilder");
            ParamChecker.AssertNotNull(MergeController, "mergeController");
            ParamChecker.AssertNotNull(OriHelper, "oriHelper");
            ParamChecker.AssertNotNull(TypeInfoProvider, "typeInfoProvider");
        }

        public void AfterStarted()
        {
            DirectObjRefDirectMember = TypeInfoProvider.GetHierarchicMember(typeof(IDirectObjRef), "Direct");
        }

        public Object ProcessWrite(IPostProcessWriter writer)
        {
            ISet<Object> substitutedEntities = writer.SubstitutedEntities;
            if (substitutedEntities.Count == 0)
            {
                return null;
            }

            IDisposableCache childCache = CacheFactory.Create(CacheFactoryDirective.NoDCE);
            IServiceContext mergeContext = BeanContext.CreateService(new RegisterPhaseDelegate(delegate(IBeanContextFactory childContextFactory)
                {
                    childContextFactory.RegisterAnonymousBean(typeof(MergeHandle)).Autowireable<MergeHandle>().PropertyValue("Cache", childCache);
                }));
            try
            {
                IDictionary<Object, Int32> mutableToIdMap = writer.MutableToIdMap;
                IObjRefHelper oriHelper = OriHelper;
                MergeHandle mergeHandle = mergeContext.GetService<MergeHandle>();
                IList<Object> toMerge = new List<Object>(substitutedEntities.Count);
                foreach (Object entity in substitutedEntities)
                {
                    toMerge.Add(entity);
                    IObjRef ori = oriHelper.EntityToObjRef(entity);
                    mergeHandle.objToOriDict.Add(entity, ori);
                    Int32 id = mutableToIdMap[entity];
                    mutableToIdMap.Add(ori, id);
                }
                ICUDResult cudResult = MergeController.MergeDeep(toMerge, mergeHandle);
                if (cudResult.AllChanges.Count != 0)
                {
                    return cudResult;
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                mergeContext.Dispose();
            }
        }

        public void ProcessRead(IPostProcessReader reader)
        {
            reader.NextTag();

            ICommandTypeRegistry commandTypeRegistry = reader.CommandTypeRegistry;
            ICommandTypeExtendable commandTypeExtendable = reader.CommandTypeExtendable;
            commandTypeExtendable.RegisterOverridingCommandType(typeof(MergeArraySetterCommand), typeof(ArraySetterCommand));
            Object result = reader.ReadObject();
            commandTypeExtendable.UnregisterOverridingCommandType(typeof(MergeArraySetterCommand), typeof(ArraySetterCommand));

            if (!(result is CUDResult))
            {
                throw new Exception("Can only handle results of type '" + typeof(CUDResult).Name + "'. Result of type '"
                        + result.GetType().Name + "' given.");
            }

            ICommandBuilder commandBuilder = CommandBuilder;
            ITypeInfoItem directObjRefDirectMember = DirectObjRefDirectMember;
            CUDResult cudResult = (CUDResult)result;
            IList<IChangeContainer> changes = cudResult.AllChanges;
            for (int i = 0, size = changes.Count; i < size; i++)
            {
                IChangeContainer changeContainer = changes[i];
                if (!(changeContainer is CreateContainer))
                {
                    continue;
                }

                IObjRef ori = changeContainer.Reference;
                if (ori == null)
                {
                    continue;
                }
                else if (ori is DirectObjRef)
                {
                    IObjectFuture objectFuture = new ObjRefFuture(ori);
                    IObjectCommand setterCommand = commandBuilder.Build(commandTypeRegistry, objectFuture, ori, directObjRefDirectMember);
                    reader.AddObjectCommand(setterCommand);
                    IObjectCommand mergeCommand = commandBuilder.Build(commandTypeRegistry, objectFuture, changeContainer);
                    reader.AddObjectCommand(mergeCommand);
                }
                else
                {
                    throw new Exception("Not implemented yet");
                }
            }
        }
    }
}
