using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public abstract class AbstractPropertyConfiguration : IPropertyConfiguration
    {
        public static readonly CHashSet<String> ignoreClassNames = new CHashSet<String>(0.5f);

        static AbstractPropertyConfiguration()
        {
            //ignoreClassNames.Add(typeof(Thread)..class.getName());
            ignoreClassNames.Add(typeof(AbstractPropertyConfiguration).FullName);
            ignoreClassNames.Add(typeof(BeanContextFactory).FullName);
            ignoreClassNames.Add(typeof(LinkController).FullName);
            ignoreClassNames.Add(typeof(PropertyEmbeddedRefConfiguration).FullName);
            ignoreClassNames.Add(typeof(PropertyRefConfiguration).FullName);
            ignoreClassNames.Add(typeof(PropertyValueConfiguration).FullName);

            ignoreClassNames.AddAll(AbstractBeanConfiguration.ignoreClassNames);
        }

        public static StackFrame[] GetCurrentStackTraceCompact()
        {
            return GetCurrentStackTraceCompactIntern(null);
        }

        public static StackFrame[] GetCurrentStackTraceCompact(ISet<String> ignoreClassNames, IProperties props)
        {
            if (props == null || !Boolean.Parse(props.GetString(IocConfigurationConstants.TrackDeclarationTrace, "false")))
            {
                return null;
            }

            return GetCurrentStackTraceCompactIntern(ignoreClassNames);
        }

        protected static StackFrame[] GetCurrentStackTraceCompactIntern(ISet<String> ignoreClassNames)
        {
            StackFrame[] stes = new StackTrace().GetFrames();
            int start = 0, end = stes.Length;
            if (ignoreClassNames != null && ignoreClassNames.Count > 0)
            {
                for (int a = 0, size = stes.Length; a < size; a++)
                {
                    StackFrame ste = stes[a];
                    if (!ignoreClassNames.Contains(ste.GetMethod().DeclaringType.FullName))
                    {
                        start = a;
                        break;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int a = start, size = stes.Length; a < size; a++)
            {
                StackFrame ste = stes[a];
                String fileName = ste.GetMethod().DeclaringType.FullName;
                if (fileName.StartsWith("org.eclipse.jdt"))
                {
                    end = a;
                    break;
                }
                if (a != start)
                {
                    sb.Append('\n');
                }
                sb.Append(fileName).Append('.').Append(ste.GetMethod().Name);
                //if (ste.GetMethod().is.isNativeMethod())
                //{
                //    sb.Append("(Native Method)");
                //}
                if (ste.GetFileLineNumber() >= 0)
                {
                    sb.Append(':').Append(ste.GetFileLineNumber()).Append(')');
                }
                sb.Append(ste);
            }
            StackFrame[] newFrame = new StackFrame[end - start];
            Array.Copy(stes, start, newFrame, 0, end - start);
            return newFrame;
        }

        protected StackFrame[] declarationStackTrace;

        protected IBeanConfiguration beanConfiguration;

        public AbstractPropertyConfiguration(IBeanConfiguration beanConfiguration, IProperties props)
        {
            this.beanConfiguration = beanConfiguration;
            ParamChecker.AssertParamNotNull(beanConfiguration, "beanConfiguration");
            declarationStackTrace = GetCurrentStackTraceCompact(ignoreClassNames, props);
        }

        public abstract bool IsOptional();

        public abstract String GetBeanName();

        public abstract String GetPropertyName();

        public abstract Object GetValue();

        public StackFrame[] GetDeclarationStackTrace()
        {
            return declarationStackTrace;
        }

        public IBeanConfiguration BeanConfiguration
        {
            get
            {
                return beanConfiguration;
            }
        }
    }
}