## Contributing to Mars

If you wish to submit a feature request or bug report, please do so on the [Issues page](https://github.com/Warzone/mars/issues).

We also welcome code contributions. Follow the steps below to compile the Mars plugin.

1. Clone the latest version of the Mars repository
2. Using Java 8, compile the plugin with `$ ./gradlew shadowjar`
   1. You may need to assign executable permission for the `gradlew` binary depending on your environment (e.g. `$ chmod +x ./gradlew` on UNIX)
3. The plugin JAR can be found at `build/libs/Mars-1.0-SNAPSHOT-all.jar`
4. Follow the steps in **[Using Mars](https://github.com/Warzone/mars/tree/main/docs/USING.md)** to test the plugin

Before starting to work on sizeable contributions, we highly recommend you [create an issue](https://github.com/Warzone/mars/issues/new) for the idea or comment on the issue if it already exists. Alternatively, mention it in the `#contributing` channel on [our Discord server](https://warz.one/discord). This is to make sure others don't accidentally work on the same thing as you. If it's a new issue, it is always good to get validation of your idea before spending time on implementation. Of course, it's fine if you don't, but there's no guarantee the contribution will be accepted.

### Architecture

Mars, the Spigot plugin, depends on [Mars API](https://github.com/Warzone/mars-api). Mars API handles persistent storage, stat calculations, authorisation, etc, while Mars is the "client" on the Minecraft server. Mars depends on PGM's Java API. Mars and Mars API communicate using HTTP(s) and WebSockets. Mars API depends on Redis and Mongo (see [Mars API](https://github.com/Warzone/mars-api) for more info).
