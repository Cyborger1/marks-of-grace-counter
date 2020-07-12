# Marks of Grace Counter
This plugin counts the number of Marks of Grace
that spawn while you're doing rooftop agility.

![The plugin in action](/images/marks.png)

## Features
The plugin provides the following metrics:
* The total number of spawns.
* The current number of Marks on the ground
    * Useful for the Ardougne course.
* The time passed since the last spawn.
* The estimated amount of Mark spawns per hour.
    * Appears after getting at least 2 spawns.
    
## Known Issues
* Dropping Marks on the ground yourself does
trigger a spawn for the plugin if you reload
the scene. Be careful!
    * Shift-click the overlay and select the
    __Clear__ option to reset the counters.
* The plugin assumes you always eventually pick
up the Marks before they despawn. If a mark
despawns while you're not there to see it,
the "Marks on Ground" counter will get desynced.
    * Pick up any Marks in the area and select
    the __Reset Ground Counter__ option on the
    overlay to fix this.