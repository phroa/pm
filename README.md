# pm

Find and install Sponge plugins easily.

#### Usage:

    /ore [-c category] [-s sorting] [-l limit] - List plugins by category
    /ore info <pluginid> - Get info on a plugin
    /ore install <pluginid> [version|recommended|latest] - Install a plugin

Categories:

    admin / Administrator Tools
    chat / Chat Tools
    dev / Developer Tools
    economy / Economy
    gameplay / Gameplay
    games / Games
    protection / Protection
    role / Role Playing
    world / World Management
    misc / Miscellaneous
    undefined / Undefined

Sorting methods:

    stars / Most Stars
    downloads / Most Downloads
    views / Most Views
    new / Newest
    recent / updated / Recently Updated

Most everything is done through clickable menus from the main `/ore` command.  Dependency versions are checked and displayed, but that won't stop you from installing a plugin.

Click on version numbers to download that version, or on project channel tags to browse that channel.

#### Permissions:

Just one, `pm.base`.

#### Details

The only web requests performed are to the Ore API, and never without user direction.  Plugin .jars will be downloaded and saved to the mods folder upon request, that's the nature of the plugin. They're not executed, and users are warned prior to downloading unapproved files.

Updates are not checked automatically for this plugin or any others.

#### LICENSE

This plugin is made available under GPLv2 or GPLv3 at your option.  Copies are included in the source.

--------

![Running /ore and showing info about a plugin](http://i.imgur.com/uT93m2O.png)
