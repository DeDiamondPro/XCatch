# XCatch
### Efficient yet effective plugin to catch X-rayers for Minecraft 1.13-1.19.
XCatch analyzes how a player is mining by looking how many turns they make and how many rare ores they find in a small period of time.
## Features
- Send an alert to staff after a configurable amount of flags.

   ![image](https://user-images.githubusercontent.com/67508414/163673854-b3b1c3c1-1f9f-4c47-92e5-34d28ba77030.png)
- Automatically ban players after a configurable amount of flags (off by default).
- See flags and bans of online or offline players in a gui.

   ![image](https://user-images.githubusercontent.com/67508414/162445888-556cf94e-2389-4887-ac4c-585e1af2b859.png)![image](https://user-images.githubusercontent.com/67508414/163673833-9c7a7153-0f3c-4d47-9a2a-7a709e496427.png) 
- Ability to specify multiple ban times depending on how many previous bans a player has
- Log flags in a Discord channel with DiscordSRV
- Ability to execute commands after a number of flags
## Why use this over other plugins
- Plugins that hide ores (like Orebfuscator) are generally very CPU intensive, while XCatch is not and those plugins can be bypassed if the player knows the seed, which can be easily obtained with a seedcracker mod, while XCatch cannot be easily bypassed.
- Plugins that announce when a player has mined a lot of ores (like OreAnnouncer) have way more false flags when players are just lucky or when they find a big ore vein
## Commands
- /xcatch help, shows all commands.
- /xcatch view \[\<player>], shows flags of all players or the player specified.
- /xcatch info, get some statistics about XCatch on your server
  
  ![image](https://user-images.githubusercontent.com/67508414/162447605-1f0d2fe2-cb74-4b46-b24d-4100e2a6d5d9.png)
- /xcatch reload, reload the XCatch config.
- /xcatch debug \<player>, get some debug stats of a player.
## Permissions
- xcatch.alert: Players with this permission get notified of a flag.
- xcatch.command: Players with this permission get access to the XCatch command
- xcatch.bypass: Players with this permission bypass all XCatch checks
- xcatch.noban: Players with this permission will still get flagged as normal, but XCatch won't automtically ban them
## FAQ
Q: XCatch isn't detecting me Xraying
  
A: **Make sure you don't have the XCatch.bypass permission**

Q: How do I configure XCatch?

A: The configuration file is in the `XCatch` folder an is named `config.yml`, there are clear descriptions of what everything does in the config file.
