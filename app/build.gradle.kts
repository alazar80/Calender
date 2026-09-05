// ─────────────────────────────────────────────────────────────────────────────
// Imports — before anything else
// ─────────────────────────────────────────────────────────────────────────────
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import java.io.StringReader
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Batik on the classpath for the buildscript
// ─────────────────────────────────────────────────────────────────────────────
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.xmlgraphics:batik-transcoder:1.14")
        classpath("org.apache.xmlgraphics:batik-codec:1.14")
    }
}

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.calender"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.calender"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        resources {
            excludes += "license/**"
        }
    }
}

dependencies {
    implementation(libs.work.runtime)
    implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
    implementation("org.apache.xmlgraphics:batik-codec:1.14")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation("joda-time:joda-time:2.10.14")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// ─────────────────────────────────────────────────────────────────────────────
// Task: generateCalendarIcons
// ─────────────────────────────────────────────────────────────────────────────
tasks.register("generateCalendarIcons") {
    group = "asset generation"
    description = "Renders svg-calendar/calendar.template.svg → mipmap/ic_calendar_XX.png"

    doLast {
        val tplFile = rootDir.resolve("svg-calendar/calendar.template.svg")
        println(">> Looking for SVG template at: ${tplFile.absolutePath} (exists=${tplFile.exists()})")
        require(tplFile.exists()) {
            "SVG template not found at ${tplFile.absolutePath}"
        }
        val tpl = tplFile.readText()
        val pngDir = file("src/main/res/mipmap-anydpi-v26").apply { mkdirs() }

        for (d in 1..31) {
            val date = LocalDate.of(2025, 7, d)
            val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val month = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .uppercase(Locale.ENGLISH)
            val dd = "%02d".format(d)

            val svg = tpl
                .replace("{{WEEKDAY}}", weekday)
                .replace("{{MONTH}}", month)
                .replace("{{DAY}}", dd)

            PNGTranscoder().apply {
                addTranscodingHint(PNGTranscoder.KEY_WIDTH, 432f)
                addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 432f)
            }.also { transcoder ->
                val input = TranscoderInput(StringReader(svg))
                val out = pngDir.resolve("ic_calendar_$dd.png").outputStream()
                transcoder.transcode(input, TranscoderOutput(out))
                out.close()
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("generateCalendarIcons")
}
