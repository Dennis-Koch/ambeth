package com.koch.ambeth.security;
// package com.koch.ambeth.security;
//
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
//
// import org.junit.Assert;
// import org.junit.Test;
//
// import com.koch.ambeth.exception.RuntimeExceptionUtil;
// import com.koch.ambeth.privilege.model.IPrivilege;
// import com.koch.ambeth.privilege.model.IPropertyPrivilege;
// import com.koch.ambeth.privilege.model.ITypePropertyPrivilege;
// import com.koch.ambeth.privilege.model.impl.PropertyPrivilegeImpl;
// import com.koch.ambeth.privilege.model.impl.SimplePrivilegeImpl;
// import com.koch.ambeth.privilege.model.impl.TypePropertyPrivilegeImpl;
// import com.koch.ambeth.testutil.AbstractIocTest;
//
// public class PrivilegeSerializationTest extends AbstractIocTest
// {
// protected ObjectInputStream readFrom(Object... objects)
// {
// try
// {
// ByteArrayOutputStream bos = new ByteArrayOutputStream();
// ObjectOutputStream oos = new ObjectOutputStream(bos);
//
// for (Object obj : objects)
// {
// oos.writeObject(obj);
// }
// oos.flush();
//
// return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
// }
// catch (Throwable e)
// {
// throw RuntimeExceptionUtil.mask(e);
// }
// }
//
// @Test
// public void typePropertyPrivilegeImpl() throws Throwable
// {
// ITypePropertyPrivilege typePropertyPrivilege = TypePropertyPrivilegeImpl.create(null,
// Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
//
// ObjectInputStream ois = readFrom(typePropertyPrivilege);
// ITypePropertyPrivilege typePropertyPrivilege_read = (ITypePropertyPrivilege) ois.readObject();
//
// Assert.assertSame(typePropertyPrivilege, typePropertyPrivilege_read);
// }
//
// @Test
// public void propertyPrivilegeImpl() throws Throwable
// {
// IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.create(true, false, true, false);
//
// ObjectInputStream ois = readFrom(propertyPrivilege);
// IPropertyPrivilege propertyPrivilege_read = (IPropertyPrivilege) ois.readObject();
//
// Assert.assertSame(propertyPrivilege, propertyPrivilege_read);
// }
//
// @Test
// public void simplePrivilegeImpl() throws Throwable
// {
// IPrivilege privilege = SimplePrivilegeImpl.create(false, false, true, true, true);
//
// ObjectInputStream ois = readFrom(privilege);
// IPrivilege privilege_read = (IPrivilege) ois.readObject();
//
// Assert.assertSame(privilege, privilege_read);
// Assert.assertSame(privilege.getDefaultPropertyPrivilegeIfValid(),
// privilege_read.getDefaultPropertyPrivilegeIfValid());
// }
// }
