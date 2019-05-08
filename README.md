# Warframe Server Manager tools
Tools for managing your warframe dedicated servers.
![Servers](https://raw.githubusercontent.com/Spiedie/WarframeServerManager/master/Media/readme-servers.png)

Start and manage your Warframe PvP servers with these tools! Currently there are two tools, the Config Manager and the Allocator.

#### Config Manager
The config manager is a simple tool to set up and run Warframe dedicated server instances. The most noteworthy features are
* Setting the dedicated server config, with a simple and easy interface (that remembers your settings)
* Patching Warframe
* Starting server instances
![ConfigManager](https://raw.githubusercontent.com/Spiedie/WarframeServerManager/master/Media/readme-configmanagerstarter.png) 

#### Allocator
The allocator is a tool that automatically handles starting and stopping servers for you. The more players are playing a game mode, the more server instances will be started for that mode. The most popular game mode will automatically get the most instances.

The allocator can also send info about what is running to a [Discord bot](https://discord.me/conclave) (for now, it is required to provide your ip to have your servers included), allowing people to get an idea of server activity per region. This is completely optional.
![Server activity](https://raw.githubusercontent.com/Spiedie/WarframeServerManager/master/Media/readme-bot.png)


## Installation and use

#### Requirements
Both tools need Java 8 to be installed. You can find the tools in the Release folder. The estimated minimum requirements are:
* ~500-700 MB ram per server instance + OS ram requirements
* ~1 core per server instance
* ~25GB disk space per 6 instances you want to run

To have your servers log in under your account, you have to tell it what email to login with. You can simply log into warframe once and quit to have this set up for you. To do this manually, go to %localappdata%\Warframe and edit the EE.cfg file and modify the LotusDedicatedServerAccountSettings as follows:

```
[LotusDedicatedServerAccountSettings,/Lotus/Types/Game/DedicatedServerAccountSettings]
email=youremail
```

#### Config Manager
Run it like any other java program. If java is installed properly, double clicking the .jar file should do.
#### Allocator
Some steps are needed before you can run the allocator.
* Set the dedicated server config ("Set config" button in the config manager)
* run the WFAllocSetup (Release/WFAllocSetup/WFAllocSetup.jar), provide data and hit the "Setup" button
* run the WFAllocManager.cmd (Release/WFAllocManagerLocal/WFAllocManager.cmd)

## Tips
* You don't need a graphics card to run the allocator, it works fine on servers or a VPS with remote desktop.
* The setup tool can calculate the estimated maximum number of 60fps server instances your system can run.

## Known issues
If you get errors running the servers, ensure that "VC Redistributable 2015" is installed.
The allocator doesn't work with the steam version, get the warframe installer from https://www.warframe.com/download.
Some VPS providers are banned from warframe due to attacks on warframe servers. Dev: "that's why we can't have nice things".
Maximum server estimates can be (very) broken somtimes, especially in virtual environments.

