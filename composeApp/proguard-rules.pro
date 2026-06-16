# Coil 3
-keep class coil3.** { *; }

# KMP Serialization
-keepattributes Annotation, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Koin
-keep class org.koin.** { *; }
