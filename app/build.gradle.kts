plugins { id("com.android.application") }
val devDebugKey = file("signing/dev-debug.keystore")
android {
    namespace = "com.wispandco.wallpaper"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.wispandco.wallpaper"
        minSdk = 29
        targetSdk = 36
        versionCode = 5
        versionName = "0.4-context-engine"
    }
    // CI generates this test-only key and restores it from Actions cache.
    // Local builds can continue using the default Android debug key.
    signingConfigs {
        if (devDebugKey.exists()) {
            create("wispDev") {
                storeFile = devDebugKey
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }
    buildTypes {
        getByName("debug") {
            if (devDebugKey.exists()) signingConfig = signingConfigs.getByName("wispDev")
        }
    }
}
