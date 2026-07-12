plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.android.library")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

val MODULE_PACKAGE_NAME: String by project
val MODULE_VERSION_NUMBER: String by project

group = MODULE_PACKAGE_NAME
version = MODULE_VERSION_NUMBER

repositories {
    google()
    mavenCentral()
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    js(IR) {
        browser { }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "appauth_kotlin_testing"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

android {
    compileSdk = 36
    namespace = "$MODULE_PACKAGE_NAME.appauth.testing"
    defaultConfig {
        minSdk = 23
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(MODULE_PACKAGE_NAME, "appauth-kotlin-testing", MODULE_VERSION_NUMBER)
    pom {
        name.set("appauth-kotlin-testing")
        description.set("Test fakes and helpers for appauth-kotlin")
        url.set("https://github.com/yet300/AppAuth-Kotlin")
    }
}
