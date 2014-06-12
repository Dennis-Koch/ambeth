package de.osthus.ambeth.privilege.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum PrivilegeEnum
{
	NONE, CREATE_ALLOWED, UPDATE_ALLOWED, DELETE_ALLOWED, READ_ALLOWED, EXECUTE_ALLOWED;
}
