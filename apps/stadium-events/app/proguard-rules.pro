# Room (pulled in by WorkManager, which runs the evening reminder and Glance's widget) creates its
# generated *_Impl databases by reflection. Room's own rule keeps the class but not its constructor,
# which R8's strict full mode (default since AGP 9) then removes, crashing the app on start.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
