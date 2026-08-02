# Hilt / Dagger generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}

# Room entities (reflection-free at runtime, but keep for schema export tooling)
-keep class com.siroha.core.database.entity.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}

# AppWidget host classes must not be renamed (system binds by class name)
-keep class com.siroha.feature.widgets.** { *; }
