package com.nan.asmplugin.asm;

import com.nan.asmplugin.util.Logger;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LifeCycleMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;

    public LifeCycleMethodVisitor(MethodVisitor methodVisitor, String className, String methodName) {
        super(Opcodes.ASM7, methodVisitor);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        Logger.log("LifeCycleMethodVisitor visitCode");

        mv.visitLdcInsn("TAG");
        mv.visitLdcInsn(className + "---->" + methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
    }
}
