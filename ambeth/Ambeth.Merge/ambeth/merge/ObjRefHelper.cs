using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Merge
{
    public class ObjRefHelper : IObjRefHelper
    {
        [Autowired]
        public ICompositeIdFactory CompositeIdFactory { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        public IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle)
        {
            return ExtractObjRefList(objValue, mergeHandle, null, null);
        }

        public IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList)
        {
            return ExtractObjRefList(objValue, mergeHandle, null, null);
        }

        public IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle,
               IList<IObjRef> targetOriList, EntityCallback entityCallback)
        {
            if (objValue == null)
            {
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>(0);
                }
                return targetOriList;
            }
            if (objValue is IList)
            {
                IList list = (IList)objValue;
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>(list.Count);
                }

                for (int a = 0, size = list.Count; a < size; a++)
                {
                    Object objItem = list[a];
                    targetOriList.Add(GetCreateObjRef(objItem, mergeHandle));
                    if (entityCallback != null)
                    {
                        entityCallback(objItem);
                    }
                }
            }
            else if (objValue is IEnumerable)
            {
                IEnumerator objEnumerator = ((IEnumerable)objValue).GetEnumerator();
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>();
                }
                while (objEnumerator.MoveNext())
                {
                    Object objItem = objEnumerator.Current;
                    targetOriList.Add(GetCreateObjRef(objItem, mergeHandle));
                    if (entityCallback != null)
                    {
                        entityCallback(objItem);
                    }
                }
            }
            else
            {
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>(1);
                }
                targetOriList.Add(GetCreateObjRef(objValue, mergeHandle));
                if (entityCallback != null)
                {
                    entityCallback(objValue);
                }
            }
            return targetOriList;
        }

        public IList<IObjRef> ExtractObjRefList(Object objValue,
               IObjRefProvider oriProvider, IList<IObjRef> targetOriList, EntityCallback entityCallback)
        {
            if (objValue == null)
            {
                return new List<IObjRef>(0);
            }
            if (objValue is IList)
            {
                IList list = (IList)objValue;
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>(list.Count);
                }

                for (int a = 0, size = list.Count; a < size; a++)
                {
                    Object objItem = list[a];
                    targetOriList.Add(GetCreateObjRef(objItem, oriProvider));
                    if (entityCallback != null)
                    {
                        entityCallback(objItem);
                    }
                }
            }
            else if (objValue is IEnumerable)
            {
                IEnumerator objEnumerator = ((IEnumerable)objValue).GetEnumerator();
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>();
                }
                while (objEnumerator.MoveNext())
                {
                    Object objItem = objEnumerator.Current;
                    targetOriList.Add(GetCreateObjRef(objItem, oriProvider));
                    if (entityCallback != null)
                    {
                        entityCallback(objItem);
                    }
                }
            }
            else
            {
                if (targetOriList == null)
                {
                    targetOriList = new List<IObjRef>(1);
                }
                targetOriList.Add(GetCreateObjRef(objValue, oriProvider));
                if (entityCallback != null)
                {
                    entityCallback(objValue);
                }
            }
            return targetOriList;
        }

        public IObjRef GetCreateObjRef(Object obj, IObjRefProvider oriProvider)
        {
            if (obj == null)
            {
                return null;
            }
            if (obj is IObjRef)
            {
                return (IObjRef)obj;
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();
            return oriProvider.GetORI(obj, metaData);
        }

        public IObjRef GetCreateObjRef(Object obj, MergeHandle mergeHandle)
        {
            if (obj == null)
            {
                return null;
            }
            IObjRef ori = null;
            IDictionary<Object, IObjRef> objToOriDict = mergeHandle != null ? mergeHandle.objToOriDict : null;
            if (objToOriDict != null)
            {
                ori = DictionaryExtension.ValueOrDefault(objToOriDict, obj);
            }
            if (ori != null)
            {
                return ori;
            }
            if (obj is IObjRef)
            {
                return (IObjRef)obj;
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();

            Object keyValue = metaData.IdMember.GetValue(obj, false);
            if (keyValue == null || mergeHandle != null && mergeHandle.HandleExistingIdAsNewId)
            {
                IDirectObjRef dirOri = new DirectObjRef(metaData.EntityType, obj);
                if (keyValue != null)
                {
                    dirOri.Id = keyValue;
                }
                ori = dirOri;
            }
            else
            {
                Member versionMember = metaData.VersionMember;
                ori = new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, keyValue, versionMember != null ? versionMember.GetValue(obj, true) : null);
            }
            if (objToOriDict != null)
            {
                objToOriDict.Add(obj, ori);

                IDictionary<IObjRef, Object> oriToObjDict = mergeHandle != null ? mergeHandle.oriToObjDict : null;
                if (oriToObjDict != null && !oriToObjDict.ContainsKey(ori))
                {
                    oriToObjDict.Add(ori, obj);
                }
            }
            return ori;
        }

        public IObjRef EntityToObjRef(Object entity)
        {
            return EntityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, ((IEntityMetaDataHolder)entity).Get__EntityMetaData());
        }

        public IObjRef EntityToObjRef(Object entity, bool forceOri)
        {
            return EntityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, ((IEntityMetaDataHolder)entity).Get__EntityMetaData(), forceOri);
        }

        public IObjRef EntityToObjRef(Object entity, sbyte idIndex)
        {
            return EntityToObjRef(entity, idIndex, ((IEntityMetaDataHolder)entity).Get__EntityMetaData());
        }

        public IObjRef EntityToObjRef(Object entity, IEntityMetaData metaData)
        {
            return EntityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
        }

        public IObjRef EntityToObjRef(Object entity, sbyte idIndex, IEntityMetaData metaData)
        {
            return EntityToObjRef(entity, idIndex, metaData, false);
        }

        public IObjRef EntityToObjRef(Object entity, sbyte idIndex, IEntityMetaData metaData, bool forceOri)
        {
            Object id;
            Object version;
            Member versionMember = metaData.VersionMember;
            if (entity is AbstractCacheValue)
            {
                AbstractCacheValue cacheValue = (AbstractCacheValue)entity;
                if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
                {
                    id = cacheValue.Id;
                }
                else
                {
                    id = CompositeIdFactory.CreateIdFromPrimitives(metaData, idIndex, cacheValue);
                }
                version = cacheValue.Version;
            }
            else
            {
                id = metaData.GetIdMemberByIdIndex(idIndex).GetValue(entity, false);
                version = versionMember != null ? versionMember.GetValue(entity, false) : null;
            }
            IObjRef ori;

            if (id != null || forceOri)
            {
                Type idType = metaData.GetIdMemberByIdIndex(idIndex).ElementType;
                id = ConversionHelper.ConvertValueToType(idType, id);

                if (versionMember != null)
                {
                    Type versionType = metaData.VersionMember.ElementType;
                    version = ConversionHelper.ConvertValueToType(versionType, version);
                }
                ori = new ObjRef(metaData.EntityType, idIndex, id, version);
            }
            else
            {
                ori = new DirectObjRef(metaData.EntityType, entity);
            }

            return ori;
        }

        public IList<IObjRef> EntityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData)
        {
            int alternateIdCount = metaData.GetAlternateIdCount();
            IList<IObjRef> allOris = new List<IObjRef>();

            Type entityType = metaData.EntityType;
            // Convert id and version to the correct metadata type
            Member versionMember = metaData.VersionMember;
            if (versionMember != null && version != null)
            {
                version = ConversionHelper.ConvertValueToType(versionMember.RealType, version);
            }
            if (id != null)
            {
                id = ConversionHelper.ConvertValueToType(metaData.IdMember.RealType, id);
                allOris.Add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
            }
            if (alternateIdCount > 0)
            {
                Member[] alternateIdMembers = metaData.AlternateIdMembers;

                Member[] primitiveMembers = metaData.PrimitiveMembers;
                for (int a = primitiveMembers.Length; a-- > 0; )
                {
                    Member primitiveMember = primitiveMembers[a];
                    for (int b = alternateIdMembers.Length; b-- > 0; )
                    {
                        Member alternateIdMember = alternateIdMembers[b];
                        if (alternateIdMember == primitiveMember)
                        {
                            Object alternateId = primitives[a];
                            if (alternateId == null)
                            {
                                // The current member is an alternate id. But alternate ids are not mandatorily not-null
                                // If they are not specified, they are simply ignored
                                continue;
                            }
                            alternateId = ConversionHelper.ConvertValueToType(alternateIdMember.RealType, alternateId);
                            allOris.Add(new ObjRef(entityType, (sbyte)b, alternateId, version));
                            break;
                        }
                    }
                }
            }
            return allOris;
        }

        public IList<IObjRef> EntityToAllObjRefs(Object entity)
        {
            return EntityToAllObjRefs(entity, ((IEntityMetaDataHolder)entity).Get__EntityMetaData());
        }

        public IList<IObjRef> EntityToAllObjRefs(Object entity, IEntityMetaData metaData)
        {
            int alternateIdCount = metaData.GetAlternateIdCount();
            IList<IObjRef> allOris = new List<IObjRef>();

            IObjRef reference = EntityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
            if (reference.Id != null)
            {
                allOris.Add(reference);
            }
            for (int i = alternateIdCount; i-- > 0; )
            {
                reference = EntityToObjRef(entity, (sbyte)i, metaData);
                if (reference.Id != null)
                {
                    allOris.Add(reference);
                }
            }

            return allOris;
        }
    }
}
