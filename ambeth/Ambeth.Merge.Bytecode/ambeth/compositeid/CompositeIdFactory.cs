using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdFactory : ICompositeIdFactory, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            if (BytecodeEnhancer == null)
            {
                Log.Debug("No bytecodeEnhancer specified: Composite ID feature deactivated");
            }
        }

        public ITypeInfoItem CreateCompositeIdMember(IEntityMetaData metaData, ITypeInfoItem[] idMembers)
        {
            return CreateCompositeIdMember(metaData.EntityType, idMembers);
        }

        public ITypeInfoItem CreateCompositeIdMember(Type entityType, ITypeInfoItem[] idMembers)
        {
            if (BytecodeEnhancer == null)
            {
                throw new NotSupportedException("No bytecodeEnhancer specified");
            }
            StringBuilder nameSB = new StringBuilder();
            // order does matter here
            for (int a = 0, size = idMembers.Length; a < size; a++)
            {
                String name = idMembers[a].Name;
                if (a > 0)
                {
                    nameSB.Append('&');
                }
                nameSB.Append(name);
            }
            Type compositeIdType = BytecodeEnhancer.GetEnhancedType(typeof(Object), new CompositeIdEnhancementHint(idMembers));
            return new CompositeIdTypeInfoItem(entityType, compositeIdType, nameSB.ToString(), idMembers);
        }

        public Object CreateCompositeId(IEntityMetaData metaData, ITypeInfoItem compositeIdMember, params Object[] ids)
        {
            IConversionHelper conversionHelper = this.ConversionHelper;
            CompositeIdTypeInfoItem cIdTypeInfoItem = (CompositeIdTypeInfoItem)compositeIdMember;
            ITypeInfoItem[] members = cIdTypeInfoItem.Members;
            for (int a = ids.Length; a-- > 0; )
            {
                Object id = ids[a];
                Object convertedId = conversionHelper.ConvertValueToType(members[a].RealType, id);
                if (convertedId != id)
                {
                    ids[a] = convertedId;
                }
            }
            return cIdTypeInfoItem.GetRealTypeConstructorAccess().Invoke(ids);
        }

        public Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives)
        {
            int[][] alternateIdMemberIndicesInPrimitives = metaData.AlternateIdMemberIndicesInPrimitives;
            int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

            if (compositeIndex.Length == 1)
            {
                return primitives[compositeIndex[0]];
            }
            ITypeInfoItem compositeIdMember = metaData.AlternateIdMembers[idIndex];
            Object[] ids = new Object[compositeIndex.Length];
            for (int a = compositeIndex.Length; a-- > 0; )
            {
                ids[a] = primitives[compositeIndex[a]];
            }
            return CreateCompositeId(metaData, compositeIdMember, ids);
        }

        public Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue)
        {
            int[][] alternateIdMemberIndicesInPrimitives = metaData.AlternateIdMemberIndicesInPrimitives;
            int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

            if (compositeIndex.Length == 1)
            {
                return cacheValue.GetPrimitive(compositeIndex[0]);
            }
            ITypeInfoItem compositeIdMember = metaData.AlternateIdMembers[idIndex];
            Object[] ids = new Object[compositeIndex.Length];
            for (int a = compositeIndex.Length; a-- > 0; )
            {
                ids[a] = cacheValue.GetPrimitive(compositeIndex[a]);
            }
            return CreateCompositeId(metaData, compositeIdMember, ids);
        }
    }
}