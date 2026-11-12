plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    layout.buildDirectory.set(
        file("${System.getProperty("java.io.tmpdir")}/ActiveStudentBuild/${project.name}")
    )
}
