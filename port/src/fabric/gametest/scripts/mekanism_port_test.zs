// Copied into the GameTest run's scripts/ folder when running with -Ptweaker=crt (see build.gradle).
// Exercises Mekanism's CraftTweaker integration: adds an enrichment recipe the GameTest then looks up.
<recipetype:mekanism:enriching>.addRecipe("port_test_enriching", <item:minecraft:diamond>, <item:minecraft:emerald>);
