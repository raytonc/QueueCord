# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.raytonc.queuecord.**$$serializer { *; }
-keepclassmembers class com.raytonc.queuecord.** {
    *** Companion;
}
-keepclasseswithmembers class com.raytonc.queuecord.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data models
-keep class com.raytonc.queuecord.model.** { *; }
-keep class com.raytonc.queuecord.service.DiscordWebhookPayload { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**
-keepclassmembers class * implements javax.net.ssl.SSLSocketFactory {
    private javax.net.ssl.SSLSocketFactory delegate;
}

# DataStore
-keep class androidx.datastore.*.** { *; }