package com.nan.asmplugin.asm;

import com.nan.asmplugin.util.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LifecycleClassVisitor extends ClassVisitor {

    private String className;
    private String superClassName;

    public LifecycleClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
        superClassName = superName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Logger.log("LifecycleClassVisitor visitMethod className=" + className + " superClassName=" + superClassName + " methodName=" + name);

        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if (superClassName.equals("androidx/appcompat/app/AppCompatActivity") && name.startsWith("onCreate")) {
            return new LifeCycleMethodVisitor(methodVisitor, className, name);
        }
        return methodVisitor;
    }
}
