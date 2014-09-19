using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Template
{
    public class EmbeddedMemberTemplate
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired(Optional = true)]
        public IBytecodePrinter BytecodePrinter { protected get; set; }

        protected readonly SmartCopyMap<Type, ConstructorInfo> typeToEmbbeddedParamConstructorMap = new SmartCopyMap<Type, ConstructorInfo>(0.5f);

        public Object CreateEmbeddedObject(Type embeddedType, Type entityType, Object parentObject, String memberPath)
        {
            Type enhancedEmbeddedType = BytecodeEnhancer.GetEnhancedType(embeddedType, new EmbeddedEnhancementHint(entityType, parentObject.GetType(),
                    memberPath));
            ConstructorInfo embeddedConstructor = GetEmbeddedParamConstructor(enhancedEmbeddedType, parentObject.GetType());
            Object[] constructorArgs = new Object[] { parentObject };
            return embeddedConstructor.Invoke(constructorArgs);
        }

        protected ConstructorInfo GetEmbeddedParamConstructor(Type embeddedType, Type parentObjectType)
        {
            ConstructorInfo constructor = typeToEmbbeddedParamConstructorMap.Get(embeddedType);
            if (constructor == null)
            {
                try
                {
                    constructor = embeddedType.GetConstructor(new Type[] { parentObjectType });
                }
                catch (Exception e)
                {
                    if (BytecodePrinter != null)
                    {
                        throw RuntimeExceptionUtil.Mask(e, BytecodePrinter.ToPrintableBytecode(embeddedType));
                    }
                    throw;
                }
                typeToEmbbeddedParamConstructorMap.Put(embeddedType, constructor);
            }
            return constructor;
        }
    }
}