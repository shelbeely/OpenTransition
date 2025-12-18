# Wear OS ProGuard Rules

# Keep Wearable Data Layer classes
-keep class com.google.android.gms.wearable.** { *; }
-keep interface com.google.android.gms.wearable.** { *; }

# Keep shared models
-keep class com.shelbeely.opentransition.shared.** { *; }

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data model classes
-keep class com.shelbeely.opentransition.shared.models.** { *; }
