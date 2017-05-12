using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using System.Collections;
using System.Collections.ObjectModel;

namespace De.Osthus.Ambeth.Xml
{

    public class XmlTypeNotFoundException : Exception
    {
        public String XmlType { get; private set; }

        public String Namespace { get; private set; }

        public XmlTypeNotFoundException(String xmlType, String ns) : base("No type found: Name=" + xmlType + " Namespace=" + ns)
        {
            this.XmlType = xmlType;
            this.Namespace = ns;
        }
    }
}