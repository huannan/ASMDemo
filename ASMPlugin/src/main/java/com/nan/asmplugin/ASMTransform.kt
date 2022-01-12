package com.nan.asmplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.nan.asmplugin.asm.LifecycleClassVisitor
import com.nan.asmplugin.util.log
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.*
import java.util.zip.*

internal class ASMTransform : Transform() {

    override fun getName(): String {
        return "ASMTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        if (!isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                transformDirectoryFiles(directoryInput, transformInvocation.outputProvider)
            }
            transformInput.jarInputs.forEach { jarInput ->
                transformJarFiles(jarInput, transformInvocation.outputProvider)
            }
        }
    }

    private fun transformDirectoryFiles(directoryInput: DirectoryInput, outputProvider: TransformOutputProvider) {
        directoryInput.file
            .walkTopDown()
            .filter { file ->
                file.isFile && file.absolutePath.endsWith(".class")
            }
            .forEach { file ->
                log("transformDirectoryFiles file=${file.absolutePath}")
                FileInputStream(file).use { classInputStream ->
                    val byteArray = transformClass(classInputStream)
                    file.writeBytes(byteArray)
                }
            }

        val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    private fun transformJarFiles(jarInput: JarInput, outputProvider: TransformOutputProvider) {
        if (!jarInput.file.absolutePath.endsWith(".jar")) {
            log("transformJarFiles not jar file=${jarInput.file.absolutePath}")
            return
        }

        val tempFile = File(jarInput.file.parentFile, "${jarInput.file.name}.temp")
            .also {
                it.createNewFile()
            }

        log("transformJarFiles file=${jarInput.file.absolutePath}")

        JarFile(jarInput.file).use { jarFile ->
            JarOutputStream(FileOutputStream(tempFile)).use { jarOutputStream ->
                jarFile.entries().iterator().forEach { jarEntry ->
                    val zipEntry = ZipEntry(jarEntry.name)
                    jarFile.getInputStream(jarEntry).use { inputStream ->
                        if (jarEntry.name.endsWith(".class")) {
                            jarOutputStream.putNextEntry(zipEntry)
                            val byteArray = transformClass(inputStream)
                            jarOutputStream.write(byteArray)
                        } else {
                            jarOutputStream.putNextEntry(zipEntry)
                            jarOutputStream.write(IOUtils.toByteArray(inputStream))
                        }
                    }
                    jarOutputStream.closeEntry()
                }
            }
        }

        val dest = outputProvider.getContentLocation(jarInput.file.nameWithoutExtension + DigestUtils.md5Hex(jarInput.file.absolutePath), jarInput.contentTypes, jarInput.scopes, Format.JAR)
        FileUtils.copyFile(tempFile, dest)
    }

    private fun transformClass(classInputStream: InputStream): ByteArray {
        val classReader = ClassReader(classInputStream)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        classReader.accept(LifecycleClassVisitor(classWriter), ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}