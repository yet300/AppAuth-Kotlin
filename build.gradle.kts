import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.android.library")
    id("io.github.frankois944.spmForKmp") version "1.4.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.vanniktech.maven.publish") version "0.30.0"
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
        publishLibraryVariants("release")
    }

    js(IR) {
        browser { }
    }

    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = MODULE_NAME
            xcf.add(this)
            isStatic = true
        }
        iosTarget.compilations {
            val main by getting {
                cinterops.create("nativeIosShared")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-utils:3.4.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("io.github.aakira:napier:2.7.1") // or latest
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }

        androidUnitTest.dependencies {
            implementation("io.mockk:mockk:1.14.6")
            implementation("androidx.test:core:1.7.0")
            implementation("org.robolectric:robolectric:4.16")
            implementation("androidx.activity:activity:1.11.0")
        }

        jsMain.dependencies {}

        iosMain.dependencies {}

        androidMain.dependencies {
            implementation("net.openid:appauth:0.11.1")
        }

        val androidUnitTest by getting {
            dependencies {
                implementation("org.robolectric:robolectric:4.15")
                implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
                implementation("io.mockk:mockk:1.13.8")
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    compileSdk = 36
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
            isReturnDefaultValues = true
            // Ensure all unit tests run with Robolectric
            all {
                it.testLogging {
                    events("passed", "skipped", "failed")
                }
            }
        }
    }
    namespace = MODULE_PACKAGE_NAME
}

swiftPackageConfig {
    create("nativeIosShared") {
        spmWorkingPath = "${projectDir.resolve("SPM")}"
        minIos = "13.0"

        dependency {
            remotePackageVersion(
                url = uri("https://github.com/openid/AppAuth-iOS.git"),
                products = {
                    add("AppAuth", exportToKotlin = true)
                    add("AppAuthCore", exportToKotlin = true)
                },
                packageName = "AppAuth-iOS",
                version = "2.0.0",
            )
        }
    }
}

ktlint {
    filter {
        exclude("*.gradle.kts")
    }
}

fun SigningExtension.whenRequired(block: () -> Boolean) {
    setRequired(block)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.value("javadoc")
}

mavenPublishing {
    val PUBLISH_DESCRIPTION: String by project
    val PUBLISH_URL: String by project

    val PUBLISH_SCM_URL: String by project
    val PUBLISH_SCM_CONNECTION: String by project
    val PUBLISH_SCM_DEVELOPERCONNECTION: String by project

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(MODULE_PACKAGE_NAME, MODULE_NAME, MODULE_VERSION_NUMBER)

    pom {
        name.set(MODULE_NAME)
        description.set(PUBLISH_DESCRIPTION)
        url.set(PUBLISH_URL)

        licenses {
            license {
                name.set("MIT License")
                url.set("http://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("trykovyura")
                name.set("Yuri")
                url.set("https://github.com/trykovyura")
            }
            developer {
                id.set("yet300")
                name.set("Ruslan")
                url.set("https://github.com/yet300")
            }
        }

        scm {
            url.set(PUBLISH_SCM_URL)
            connection.set(PUBLISH_SCM_CONNECTION)
            developerConnection.set(PUBLISH_SCM_DEVELOPERCONNECTION)
        }
    }
}

signing {
    whenRequired { gradle.taskGraph.hasTask("publish") }
    val signingKey: String? by project
    val signingPassword: String? by project
    val signingSecretKeyRingFile: String? by project

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    } else if (signingSecretKeyRingFile != null) {
        useGpgCmd()
    }

    sign(publishing.publications)
}
