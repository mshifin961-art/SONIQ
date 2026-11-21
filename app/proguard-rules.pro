# SONIQ ProGuard / R8 Rules
# Keep Kotlinx Serialization generated serializers for type-safe navigation routes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.soniq.music.**$$serializer { *; }
-keepclassmembers class com.soniq.music.** {
    *** Companion;
}
-keepclasseswithmembers class com.soniq.music.** {
    kotlinx.serialization.KSerializer serializer(...);
}
