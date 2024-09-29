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
 
The plugin can also notify you if one of your mark
stacks is about to despawn.
    
## Known Issues
* The plugin tries to remember where you dropped
marks and ignores those tiles when detecting mark
spawns, so dropping your marks on tiles where they
can spawn will lead to issues.
* The plugin assumes you always eventually pick
up the Marks before they despawn. If a mark
despawns while you're not there to see it,
the "Marks on Ground" counter will get desynced.
    * Pick up any Marks in the area and select
    the __Reset Ground Counter__ option on the
    overlay shift-click to fix this.
    * Alternatively shift-click the overlay and
    select the __Clear__ option to reset everything.
