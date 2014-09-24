using System;
using System.IO;
using System.Reflection;
using System.Reflection.Emit;
using System.Threading;

namespace De.Osthus.Ambeth.Bytecode.Core
{
    public class AmbethClassLoader
    {
        protected String moduleName = "AmbethBytecodeEnhancement.dll";

        protected readonly ModuleBuilder moduleBuilder;
        
        AssemblyBuilder assemblyBuilder;

	    public AmbethClassLoader()
	    {
            String name = GetType().Assembly.FullName.Replace('\\', ' ') + "_AMBETH_" + new Random().Next();
            AssemblyName assemblyName       = new AssemblyName { Name = name };
            AssemblyBuilderAccess access;
#if SILVERLIGHT
            access = AssemblyBuilderAccess.Run;
#else
            access = AssemblyBuilderAccess.RunAndSave;
#endif
            AssemblyBuilder assemblyBuilder = Thread.GetDomain().DefineDynamicAssembly(assemblyName, access);
            moduleBuilder = assemblyBuilder.DefineDynamicModule(moduleName, true);
            
            this.assemblyBuilder = assemblyBuilder;
	    }

        public TypeBuilder CreateNewType(TypeAttributes access, String name, Type parentType, Type[] interfaces)
        {
            return moduleBuilder.DefineType(name, access, parentType, interfaces);
        }

        public void Save()
        {
            Type[] t = assemblyBuilder.GetTypes();
#if !SILVERLIGHT
            assemblyBuilder.Save(moduleName);
#endif
        }

	    public Type DefineClass(String name, byte[] b)
	    {
            throw new NotSupportedException("TODO");
            //Type type = defineClass(name, b, 0, b.length);
            //classToContentMap.put(type, b);
            //return type;
	    }

	    public byte[] GetContent(Type type)
	    {
            throw new NotSupportedException("TODO");
            //return classToContentMap.Get(type);
	    }
    }
}
