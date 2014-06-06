using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Privilege.Model
{
    [XmlType(Name = "PrivilegeEnum", Namespace = "http://schemas.osthus.de/Ambeth")]
    public enum PrivilegeEnum
    {
	    NONE, CREATE_ALLOWED, UPDATE_ALLOWED, DELETE_ALLOWED, READ_ALLOWED
    }
}
