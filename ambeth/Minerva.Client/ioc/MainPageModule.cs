using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Minerva.View;
using De.Osthus.Minerva.Command;
using De.Osthus.Ambeth.Helloworld.Transfer;
using System;
using System.Collections.Generic;

namespace De.Osthus.Minerva.Ioc
{
    public class MainPageModule : IInitializingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<MainPage>("mainPage").Precedence(PrecedenceType.LOW);

            beanContextFactory.RegisterBean<UndoNotPersistedCommand>("command_cancel");

            beanContextFactory.RegisterBean<SaveNotPersistedCommand>("command_save").PropertyRef("ControllerService", "client.helloWorldService").PropertyValue("MethodName", "SaveTestEntities")
                .PropertyValue("MethodParameters", new Type[] { typeof(IEnumerable<TestEntity>) });

            beanContextFactory.RegisterBean<SaveNotPersistedCommand>("command_save2").PropertyRef("ControllerService", "client.helloWorldService").PropertyValue("MethodName", "SaveTestEntities2")
                .PropertyValue("MethodParameters", new Type[] { typeof(IEnumerable<TestEntity2>) });
        }
    }
}
