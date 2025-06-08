import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.1.21"
    kotlin("native.cocoapods") version "2.1.21"
    id("com.android.library")
    id("io.github.luca992.multiplatform-swiftpackage") version "2.2.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    `maven-publish`
    signing
}

val MODULE_PACKAGE_NAME: String by project
val MODULE_NAME: String by project
val MODULE_VERSION_NUMBER: String by project

group = MODULE_PACKAGE_NAME
version = MODULE_VERSION_NUMBER

repositories {
    google()
    mavenCentral()
}

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
    verify {
        onCheck.set(true)
    }
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
    }

    js(IR) {
        browser { }
    }

    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = MODULE_NAME
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-utils:3.1.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("io.github.aakira:napier:2.7.1") // or latest
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }


        jsMain.dependencies {}

        iosMain.dependencies {}


        androidMain.dependencies {
            implementation("net.openid:appauth:0.11.1")
        }
    }
}

android {
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
        manifestPlaceholders += "appAuthRedirectScheme" to "dev.gitlive"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = MODULE_PACKAGE_NAME
}

kotlin {
    cocoapods {
        ios.deploymentTarget = "7.0"

        noPodspec()
        pod("AppAuth")
    }
}

multiplatformSwiftPackage {
    packageName(MODULE_NAME)
    swiftToolsVersion("5.4")
    targetPlatforms {
        iOS { v("13") }
    }
}

ktlint {
    version.set("0.50.0")
}

fun SigningExtension.whenRequired(block: () -> Boolean) {
    setRequired(block)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.value("javadoc")
}

publishing {
    val OPEN_SOURCE_REPO: String by project
    val PUBLISH_NAME: String by project
    val PUBLISH_DESCRIPTION: String by project
    val PUBLISH_URL: String by project
    val POM_DEVELOPER_ID: String by project
    val POM_DEVELOPER_NAME: String by project
    val POM_DEVELOPER_EMAIL: String by project
    val PUBLISH_SCM_URL: String by project
    val PUBLISH_SCM_CONNECTION: String by project
    val PUBLISH_SCM_DEVELOPERCONNECTION: String by project

    repositories {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/yet300/AppAuth-Kotlin")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }

    publications.all {
        this as MavenPublication

        artifact(javadocJar)

        pom {
            name.set(PUBLISH_NAME)
            description.set(PUBLISH_DESCRIPTION)
            url.set(PUBLISH_URL)

            licenses {
                license {
                    name.set("MIT License")
                    url.set("http://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set(POM_DEVELOPER_ID)
                    name.set(POM_DEVELOPER_NAME)
                    email.set(POM_DEVELOPER_EMAIL)
                }
            }

            scm {
                url.set(PUBLISH_SCM_URL)
                connection.set(PUBLISH_SCM_CONNECTION)
                developerConnection.set(PUBLISH_SCM_DEVELOPERCONNECTION)
            }
        }
    }
}

signing {
    whenRequired { gradle.taskGraph.hasTask("publish") }
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
