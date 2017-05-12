using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Model
{
    [XmlType(Name = "ISecurityScope", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface ISecurityScope
    {
        String Name { get; }
    }
}
