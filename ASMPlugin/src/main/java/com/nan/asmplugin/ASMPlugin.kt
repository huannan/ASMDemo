package com.nan.asmplugin

import com.android.build.gradle.AppExtension
import com.nan.asmplugin.util.log
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 调试步骤
 * ./gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true
 * 启动remote debug
 */
class ASMPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        log("ASMPlugin apply")
        val extension = project.extensions.getByType(AppExtension::class.java)
        extension.registerTransform(ASMTransform())
    }
}