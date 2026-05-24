# Behavioral Firewall - Production Proguard & R8 Shrinking Rules

# 1. General Optimization & Obfuscation Parameters
-repackageclasses 'com.behavioralfirewall.obfuscated'
-allowaccessmodification

# 2. SQLCipher Native Library & Database Rules
-keep class net.sqlcipher.** { *; }
-keep interface net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# 3. Android Room Database Persistence Rules
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper
-dontwarn androidx.room.**

# 4. Dependency Injection (Hilt & Dagger) Rules
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.GeneratedComponentManager
-keep class * extends hilt_aggregated_deps.** { *; }
-keep class **_HiltModules* { *; }
-dontwarn dagger.hilt.**
-dontwarn com.google.dagger.**

# 5. Appwrite Remote Network SDK Rules
-keep class io.appwrite.models.** { *; }
-keep class io.appwrite.services.** { *; }
-dontwarn io.appwrite.**

# 6. Keep all room entities serialized fields intact to prevent DB corruptions
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
}
