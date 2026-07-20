# Hilt — keep generated entry points only
-keep,allowobfuscation class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers class * {
    @dagger.hilt.* <methods>;
    @javax.inject.Inject <init>(...);
}
