package com.koch.ambeth.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;

public class InterfaceAdder extends ClassVisitor {
    private final String[] newInterfaces;
    private final IMap<String, String> interfaceToSignature;

    public InterfaceAdder(ClassVisitor cv, ISet<String> newInterfaces) {
        super(Opcodes.ASM4, cv);
        this.newInterfaces = newInterfaces.toArray(String[]::new);
        interfaceToSignature = null;
    }

    public InterfaceAdder(ClassVisitor cv, String... newInterfaces) {
        super(Opcodes.ASM4, cv);
        this.newInterfaces = new LinkedHashSet<>(newInterfaces).toArray(String[]::new);
        interfaceToSignature = null;
    }

    public InterfaceAdder(ClassVisitor cv, Class<?>... newInterfaces) {
        super(Opcodes.ASM4, cv);
        LinkedHashSet<String> myInterfaces = new LinkedHashSet<>(newInterfaces.length);
        for (Class<?> newInterface : newInterfaces) {
            myInterfaces.add(Type.getInternalName(newInterface));
        }
        this.newInterfaces = myInterfaces.toArray(String[]::new);
        interfaceToSignature = null;
    }

    public InterfaceAdder(ClassVisitor cv, String[] signatures, Class<?>[] newInterfaces) {
        super(Opcodes.ASM4, cv);
        LinkedHashSet<String> myInterfaces = new LinkedHashSet<>(newInterfaces.length);
        IMap<String, String> interfaceToSignature = LinkedHashMap.create(newInterfaces.length);
        for (int a = 0, size = newInterfaces.length; a < size; a++) {
            Class<?> newInterface = newInterfaces[a];
            String newInterfaceName = Type.getInternalName(newInterface);
            if (!myInterfaces.add(newInterfaceName)) {
                continue;
            }
            interfaceToSignature.put(newInterfaceName, signatures[a]);
        }
        this.newInterfaces = myInterfaces.toArray(String[]::new);
        this.interfaceToSignature = interfaceToSignature;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (signature != null && interfaceToSignature != null) {
            throw new IllegalStateException("Generic type together with generic interfaces not yet supported");
        }
        LinkedHashSet<String> ints = new LinkedHashSet<>(interfaces);
        if (interfaceToSignature != null) {
            StringBuilder sb = new StringBuilder();
            String objDesc = Type.getType(Object.class).getDescriptor();
            if (signature != null) {
                sb.append(sb);
            } else {
                sb.append(objDesc);
            }
            HashSet<String> alreadyImplemented = new HashSet<>();
            Class<?> type = BytecodeBehaviorState.getState().getCurrentType();
            while (type != null && type != Object.class) {
                for (Class<?> alreadyImplementedInterface : type.getInterfaces()) {
                    String aiiName = Type.getInternalName(alreadyImplementedInterface);
                    alreadyImplemented.add(aiiName);
                }
                type = type.getSuperclass();
            }
            for (String newInterface : newInterfaces) {
                if (alreadyImplemented.contains(newInterface)) {
                    continue;
                }
                ints.add(newInterface);
                String interfaceSignature = interfaceToSignature.get(newInterface);
                if (interfaceSignature == null) {
                    interfaceSignature = objDesc;
                }
                sb.append(interfaceSignature);
            }
            signature = sb.toString();
        } else {
            ints.addAll(newInterfaces);
            Class<?> type = BytecodeBehaviorState.getState().getCurrentType();
            while (type != null && type != Object.class) {
                for (Class<?> alreadyImplementedInterface : type.getInterfaces()) {
                    String aiiName = Type.getInternalName(alreadyImplementedInterface);
                    ints.remove(aiiName);
                }
                type = type.getSuperclass();
            }
        }
        super.visit(version, access, name, signature, superName, ints.toArray(String[]::new));
    }
}
