using ClrTest.Reflection;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using System;
using System.IO;
using System.Reflection;
using System.Text;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Core
{
    public class BytecodeClassLoader : IBytecodeClassLoader, IEventListener
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        protected readonly AmbethClassLoader ambethClassLoader;

        //protected readonly WeakSmartCopyMap<Type, WeakReference> typeToContentMap = new WeakSmartCopyMap<Type, WeakReference>();

        public BytecodeClassLoader()
        {
            ambethClassLoader = new AmbethClassLoader();
            //typeToContentMap.setAutoCleanupReference(true);
        }

        public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            if (!(eventObject is ClearAllCachesEvent))
            {
                return;
            }
            //typeToContentMap.Clear();
        }

        public Type LoadClass(String typeName, byte[] content)
        {
            typeName = typeName.Replace('/', '.');
            return ambethClassLoader.DefineClass(typeName, content);
        }

        public byte[] ReadTypeAsBinary(Type type)
        {
            throw new NotImplementedException("TODO");
            //WeakReference contentR = typeToContentMap.Get(type);
            //byte[] content = null;
            //if (contentR != null)
            //{
            //    content = (byte[])contentR.Target;
            //}
            //if (content != null)
            //{
            //    return content;
            //}
            //content = ambethClassLoader.GetContent(type);
            //if (content != null)
            //{
            //    typeToContentMap.put(type, new WeakReference(content));
            //    return content;
            //}
            //String bytecodeTypeName = GetBytecodeTypeName(type);
            //InputStream is = ambethClassLoader.getResourceAsStream(bytecodeTypeName + ".class");
            //if (is == null)
            //{
            //    throw new IllegalArgumentException("No class found with name '" + type.getName() + "'");
            //}
            //try
            //{
            //    ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //    int oneByte;
            //    while ((oneByte = is.read()) != -1)
            //    {
            //        bos.write(oneByte);
            //    }
            //    content = bos.toByteArray();
            //    typeToContentMap.put(type, new WeakReference<byte[]>(content));
            //    return content;
            //}
            //finally
            //{
            //    is.close();
            //}
        }

        public void Verify(byte[] content)
        {
            throw new NotImplementedException("TODO");
            //CheckClassAdapter.verify(new ClassReader(content), ambethClassLoader, false, new PrintWriter(new LogWriter(log)));
        }

        public Type BuildTypeFromScratch(String newTypeName, StringBuilder writer, IBuildVisitorDelegate buildVisitorDelegate)
        {
            newTypeName = GetBytecodeTypeName(newTypeName);

            return BuildTypeFromParent(newTypeName, typeof(Object), writer, buildVisitorDelegate);
        }

        public Type BuildTypeFromParent(String newTypeName, Type sourceContent, StringBuilder writer, IBuildVisitorDelegate buildVisitorDelegate)
        {
            newTypeName = GetBytecodeTypeName(newTypeName);

            StringBuilder sb = new StringBuilder();

            ClassWriter cw = BeanContext.RegisterWithLifecycle(new ClassWriter(ambethClassLoader, sb)).Finish();

            IClassVisitor visitor = cw;// new SuppressLinesClassVisitor(cw);
            visitor = new LogImplementationsClassVisitor(visitor);
            //visitor = new TraceClassVisitor(visitor, sb);
            IClassVisitor wrappedVisitor = visitor;
            Type originalModifiers = BytecodeBehaviorState.State.OriginalType;
            if (originalModifiers.IsInterface || originalModifiers.IsAbstract)
            {
                wrappedVisitor = new InterfaceToClassVisitor(wrappedVisitor);
            }
            if (!PublicConstructorVisitor.HasValidConstructor())
            {
                wrappedVisitor = new PublicConstructorVisitor(wrappedVisitor);
            }
            wrappedVisitor = buildVisitorDelegate.Invoke(wrappedVisitor);

            if (Object.ReferenceEquals(wrappedVisitor, visitor))
            {
                // there seem to be no custom action to be done with the new type. So we skip type enhancement
                return null;
            }
            visitor = wrappedVisitor;

            visitor.Visit(sourceContent.Attributes, newTypeName, sourceContent, new Type[0]);

            Type content = null;
            try
            {
                visitor.VisitEnd();
                content = cw.GetCreatedType();
            }
            catch (Exception)
            {
                Log.Error(sb.ToString());
                throw;
            }
            if (content == null)
            {
                throw new Exception("A visitor did not correctly call its cascaded visitor with VisitEnd()");
            }
            ambethClassLoader.Save();
            //Verify(content);
            return content;
        }

        public String ToPrintableBytecode(Type type)
        {
            if (type == null)
            {
                return "<null>";
            }
            StringBuilder sb = new StringBuilder();

            ToPrintableByteCodeIntern(type, sb);
            return sb.ToString();
        }

        protected void ToPrintableByteCodeIntern(Type type, StringBuilder sb)
        {
            if (type.BaseType != null)// && typeof(IEnhancedType).IsAssignableFrom(type.BaseType))
            {
                // write parent classes first
                ToPrintableByteCodeIntern(type.BaseType, sb);
                sb.Append('\n');
            }
            sb.Append(type.ToString());

            StringWriter sw = new StringWriter(sb);
            ILInstructionVisitor visitor = new ReadableILStringVisitor(new ReadableILStringToTextWriter(sw));

            if (type is TypeBuilder)
            {
                MethodInstance[] methods = BytecodeBehaviorState.State.GetAlreadyImplementedMethodsOnNewType();

                foreach (MethodInstance method in methods)
                {
                    sb.Append('\n');
                    PrintAnnotations(method.Method, sb);
                    sb.Append(method.ToString()).Append('\n');

                    ILReader reader = new ILReader(method.Method);

                    reader.Accept(visitor);
                }
                return;
            }
            {
                ConstructorInfo[] constructors = type.GetConstructors(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
                MethodInfo[] methods = type.GetMethods(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
                FieldInfo[] fields = type.GetFields(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);

                foreach (FieldInfo field in fields)
                {
                    sb.Append('\n');
                    PrintAnnotations(field, sb);
                    sb.Append(field.ToString()).Append('\n');
                }

                foreach (ConstructorInfo constructor in constructors)
                {
                    sb.Append('\n');
                    PrintAnnotations(constructor, sb);
                    sb.Append(constructor.ToString()).Append('\n');

                    ILReader reader = new ILReader(constructor);

                    reader.Accept(visitor);
                }

                foreach (MethodInfo method in methods)
                {
                    sb.Append('\n');
                    PrintAnnotations(method, sb);
                    sb.Append(method.ToString()).Append('\n');

                    ILReader reader = new ILReader(method);

                    reader.Accept(visitor);
                }
            }
        }

        protected void PrintAnnotations(MemberInfo member, StringBuilder sb)
        {
            try
            {
                Object[] attributes = member.GetCustomAttributes(false);
                foreach (Object att in attributes)
                {
                    Attribute attribute = (Attribute)att;
                    sb.Append('[').Append(attribute.ToString()).Append("]\n");
                }
            }
            catch (NotSupportedException)
            {
                sb.Append("<Annotations could not be accessed due to NotSupportedException>\n");
            }
        }

        public String GetBytecodeTypeName(Type type)
        {
            return GetBytecodeTypeName(type.FullName);
        }

        protected String GetBytecodeTypeName(String typeName)
        {
            return typeName.Replace('.', '/');
        }
    }
}