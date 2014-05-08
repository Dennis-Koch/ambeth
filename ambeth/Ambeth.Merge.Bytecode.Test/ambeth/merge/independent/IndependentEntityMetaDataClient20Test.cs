using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Independent;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Testutil;

namespace De.Osthus.Ambeth.Merge.Independent
{
    [TestClass]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = rootPath + "independent-orm.xml;" + rootPath + "independent-orm2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.ValueObjectFile, Value = rootPath + "independent-vo-config.xml;" + rootPath + "independent-vo-config2.xml")]
    public class IndependentEntityMetaDataClient20Test : IndependentEntityMetaDataClientTest
    {
    }
}
