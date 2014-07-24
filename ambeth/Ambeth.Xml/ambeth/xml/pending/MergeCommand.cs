using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class MergeCommand : AbstractObjectCommand, IObjectCommand, IInitializingBean
    {
        [Autowired]
        public ICommandBuilder CommandBuilder { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }
        
        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertParamOfType(Parent, "Parent", typeof(IChangeContainer));

            ParamChecker.AssertNotNull(EntityMetaDataProvider, "MetadataProvider");
        }

        public override void Execute(IReader reader)
        {
            IPrimitiveUpdateItem[] puis;
            IRelationUpdateItem[] ruis;
            if (Parent is CreateContainer)
            {
                CreateContainer createContainer = (CreateContainer)Parent;
                puis = createContainer.Primitives;
                ruis = createContainer.Relations;
            }
            else if (Parent is UpdateContainer)
            {
                UpdateContainer updateContainer = (UpdateContainer)Parent;
                puis = updateContainer.Primitives;
                ruis = updateContainer.Relations;
            }
            else
            {
                throw new Exception("Unsupported " + typeof(IChangeContainer).Name + " of type '" + Parent.GetType().FullName + "'");
            }

            Object entity = ObjectFuture.Value;
            Type realType = entity.GetType();
            IEntityMetaData metadata = EntityMetaDataProvider.GetMetaData(realType);
            ApplyPrimitiveUpdateItems(entity, puis, metadata);

            if (ruis != null && ruis.Length > 0)
            {
                ApplyRelationUpdateItems((IObjRefContainer) entity, ruis, Parent is UpdateContainer, metadata, reader);
            }
        }

        protected void ApplyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata)
        {
            if (puis == null)
            {
                return;
            }

            foreach (IPrimitiveUpdateItem pui in puis)
            {
                String memberName = pui.MemberName;
                Object newValue = pui.NewValue;
                ITypeInfoItem member = metadata.GetMemberByName(memberName);
                member.SetValue(entity, newValue);
            }
        }

        protected void ApplyRelationUpdateItems(IObjRefContainer entity, IRelationUpdateItem[] ruis, bool isUpdate, IEntityMetaData metadata, IReader reader)
        {
            List<Object> toPrefetch = new List<Object>();
            IRelationInfoItem[] relationMembers = metadata.RelationMembers;
            foreach (IRelationUpdateItem rui in ruis)
            {
                String memberName = rui.MemberName;
                int relationIndex = metadata.GetIndexByRelationName(memberName);
			    if (ValueHolderState.INIT == entity.Get__State(relationIndex))
                {
                    throw new Exception("ValueHolder already initialized for property '" + memberName + "'");
                }

                IObjRef[] existingORIs = entity.Get__ObjRefs(relationIndex);
                IObjRef[] addedORIs = rui.AddedORIs;
                IObjRef[] removedORIs = rui.RemovedORIs;

                IObjRef[] newORIs;
                if (existingORIs.Length == 0)
                {
                    if (removedORIs != null && addedORIs.Length > 0)
                    {
                        throw new ArgumentException("Removing from empty member");
                    }
                    newORIs = addedORIs != null && addedORIs.Length > 0 ? addedORIs : ObjRef.EMPTY_ARRAY;
                }
                else
                {
                    // Set to efficiently remove entries
                    LinkedHashSet<IObjRef> existingORIsSet = new LinkedHashSet<IObjRef>(existingORIs);
                    if (removedORIs != null && removedORIs.Length > 0)
                    {
                        foreach (IObjRef removedORI in removedORIs)
                        {
                            if (!existingORIsSet.Remove(removedORI))
                            {
                                throw OptimisticLockUtil.ThrowModified(OriHelper.EntityToObjRef(entity), null, entity);
                            }
                        }
                    }
                    if (addedORIs != null && addedORIs.Length > 0)
                    {
                        foreach (IObjRef addedORI in addedORIs)
                        {
                            if (!existingORIsSet.Add(addedORI))
                            {
                                throw OptimisticLockUtil.ThrowModified(OriHelper.EntityToObjRef(entity), null, entity);
                            }
                        }
                    }
                    if (existingORIsSet.Count == 0)
                    {
                        newORIs = ObjRef.EMPTY_ARRAY;
                    }
                    else
                    {
                        newORIs = existingORIsSet.ToArray();
                    }
                }

                IRelationInfoItem member = relationMembers[relationIndex];
                if (isUpdate)
                {
                    entity.Set__ObjRefs(relationIndex, newORIs);
                    if (!entity.Is__Initialized(relationIndex))
                    {
                        DirectValueHolderRef dvhr = new DirectValueHolderRef(entity, member);
                        toPrefetch.Add(dvhr);
                    }
                }
                else
                {
                    BuildSetterCommands(entity, newORIs, member, reader);
                }
            }
            if (toPrefetch.Count > 0)
            {
                IObjectFuture objectFuture = new PrefetchFuture(toPrefetch);
                IObjectCommand command = CommandBuilder.Build(reader.CommandTypeRegistry, objectFuture, null);
                reader.AddObjectCommand(command);
            }
        }

        protected void BuildSetterCommands(Object entity, IObjRef[] newORIs, IRelationInfoItem member, IReader reader)
        {
            if (!member.IsToMany)
            {
                if (newORIs.Length == 0)
                {
                    return;
                }
                else if (newORIs.Length == 1)
                {
                    IObjectFuture objectFuture = new ObjRefFuture(newORIs[0]);
                    IObjectCommand command = CommandBuilder.Build(reader.CommandTypeRegistry, objectFuture, entity, member);
                    reader.AddObjectCommand(command);
                }
                else
                {
                    throw new ArgumentException("Multiple values for to-one relation");
                }
            }
            else
            {
                Object coll = ListUtil.CreateCollectionOfType(member.RealType, newORIs.Length);
                MethodInfo addMethod = coll.GetType().GetMethod("Add");
                Object[] parameters = new Object[1];

                bool useObjectFuture = false;
                ICommandBuilder commandBuilder = CommandBuilder;
                ICommandTypeRegistry commandTypeRegistry = reader.CommandTypeRegistry;
                foreach (IObjRef ori in newORIs)
                {
                    if (!(ori is IDirectObjRef))
                    {
                        IObjectFuture objectFuture = new ObjRefFuture(ori); ;
                        IObjectCommand command = commandBuilder.Build(commandTypeRegistry, objectFuture, coll, addMethod);
                        reader.AddObjectCommand(command);
                        useObjectFuture = true;
                        continue;
                    }

                    Object item = ((IDirectObjRef)ori).Direct;
                    if (useObjectFuture)
                    {
                        IObjectCommand command = commandBuilder.Build(commandTypeRegistry, null, coll, addMethod, item);
                        reader.AddObjectCommand(command);
                    }
                    else
                    {
                        parameters[0] = item;
                        addMethod.Invoke(coll, parameters);
                    }
                }
            }
        }
    }
}
