using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class CommandBuilder : ICommandBuilder
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }
        
        public IObjectCommand Build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, params Object[] optionals)
        {
            IObjectCommand command;
            if (parent == null)
            {
                command = BuildIntern<ResolveObjectCommand>(commandTypeRegistry, objectFuture, parent).Finish();
            }
            else if (parent.GetType().IsArray)
            {
                command = BuildIntern<ArraySetterCommand>(commandTypeRegistry, objectFuture, parent).PropertyValue("Index", optionals[0]).Finish();
            }
            else if (parent is IEnumerable && !(parent is String))
            {
                IBeanRuntime<CollectionSetterCommand> beanRuntime = BuildIntern<CollectionSetterCommand>(commandTypeRegistry, objectFuture, parent);
                if (optionals.Length > 0)
                {
                    beanRuntime.PropertyValue("AddMethod", optionals[0]);
                }
                if (optionals.Length > 1)
                {
                    beanRuntime.PropertyValue("Obj", optionals[1]);
                }
                command = beanRuntime.Finish();
            }
            else if (parent is CreateContainer || parent is UpdateContainer)
            {
                command = BuildIntern<MergeCommand>(commandTypeRegistry, objectFuture, parent).Finish();
            }
            else
            {
                command = BuildIntern<ObjectSetterCommand>(commandTypeRegistry, objectFuture, parent).PropertyValue("Member", optionals[0]).Finish();
            }

            return command;
        }

        protected IBeanRuntime<C> BuildIntern<C>(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent) where C : IObjectCommand
        {
            Type commandType = typeof(C);
            Type overridingCommandType = commandTypeRegistry.GetOverridingCommandType(commandType);
            if (overridingCommandType != null)
            {
                commandType = overridingCommandType;
            }
            IBeanRuntime<C> beanRuntime = BeanContext.RegisterBean<C>(commandType).PropertyValue("ObjectFuture", objectFuture).PropertyValue("Parent", parent);
            return beanRuntime;
        }
    }
}
