import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "${JavaVersion.VERSION_1_8}"
                freeCompilerArgs += "-Xjdk-release=${JavaVersion.VERSION_1_8}"
            }
        }
        publishLibraryVariants("release")
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                debugImplementation(libs.androidx.testManifest)
                implementation(libs.androidx.junit4)
            }
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.colormath)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }

        iosMain.dependencies {
        }

    }

    // https://stackoverflow.com/questions/78133592/kmm-project-build-error-testclasses-not-found-in-project-shared
    task("testClasses")
}

android {
    namespace = "com.godaddy.colorpicker"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }
    //https://developer.android.com/studio/test/gradle-managed-devices
    @Suppress("UnstableApiUsage")
    testOptions {
        managedDevices.devices {
            maybeCreate<ManagedVirtualDevice>("pixel5").apply {
                device = "Pixel 5"
                apiLevel = 34
                systemImageSource = "aosp"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.godaddy.colorpicker.app.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

val dokkaOutputDir = layout.buildDirectory.dir("dokka")

tasks.dokkaHtml.configure {
    outputDirectory.set(dokkaOutputDir)
}

val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

val sonatypeUsername: String? = System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername")
val sonatypePassword: String? = System.getenv("ORG_GRADLE_PROJECT_mavenCentralPassword")


afterEvaluate {
    configure<PublishingExtension> {
        publications.all {
            val mavenPublication = this as? MavenPublication

            mavenPublication?.artifactId =
                "compose-color-picker${
                    "-$name".takeUnless { "kotlinMultiplatform" in name }.orEmpty()
                }".removeSuffix("Release")
        }
    }
}

signing {
    setRequired {
        // signing is only required if the artifacts are to be published
        gradle.taskGraph.allTasks.any { PublishToMavenRepository::class == it.javaClass }
    }
    sign(configurations.archives.get())
    sign(publishing.publications)
}
publishing {

    publications.withType(MavenPublication::class) {
        groupId = "com.godaddy.colorpicker"
        artifactId = "compose-color-picker"
        version = "0.7.0"

        artifact(tasks["javadocJar"])

        pom {

            name.set("compose-color-picker")
            description.set("A compose component for picking a color")
            url.set("https://github.com/godaddy/compose-color-picker")

            licenses {
                license {
                    name.set("The MIT License (MIT)")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("godaddy")
                    name.set("GoDaddy")
                }
            }
            organization {
                name.set("GoDaddy")
            }
            scm {
                connection.set("scm:git:git://github.com/godaddy/compose-color-picker.git")
                developerConnection.set("scm:git:ssh://git@github.com/godaddy/compose-color-picker.git")
                url.set("https://github.com/godaddy/compose-color-picker")
            }
        }
    }

    repositories {
        maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}

afterEvaluate {
    tasks.getByName("publishAndroidReleasePublicationToMavenLocal") {
        dependsOn("signJsPublication")
        dependsOn("signJvmPublication")
        dependsOn("signKotlinMultiplatformPublication")
        dependsOn("signWasmJsPublication")
    }
    tasks.getByName("publishJsPublicationToMavenLocal") {
        dependsOn("signAndroidReleasePublication")
        dependsOn("signJvmPublication")
        dependsOn("signKotlinMultiplatformPublication")
        dependsOn("signWasmJsPublication")
    }
    tasks.getByName("publishJvmPublicationToMavenLocal") {
        dependsOn("signAndroidReleasePublication")
        dependsOn("signJsPublication")
        dependsOn("signKotlinMultiplatformPublication")
        dependsOn("signWasmJsPublication")
    }
    tasks.getByName("publishKotlinMultiplatformPublicationToMavenLocal") {
        dependsOn("signAndroidReleasePublication")
        dependsOn("signJsPublication")
        dependsOn("signJvmPublication")
        dependsOn("signWasmJsPublication")
    }
    tasks.getByName("publishWasmJsPublicationToMavenLocal") {
        dependsOn("signAndroidReleasePublication")
        dependsOn("signJsPublication")
        dependsOn("signJvmPublication")
        dependsOn("signKotlinMultiplatformPublication")
    }
}
