## Using Mars

### Before you use...

Mars was specifically built for use on [Warzone](https://warzone.network), so some components of Mars may be harder to configure or may not be as applicable to other communities with different needs. That being said, we are more than happy to consider Pull Requests adding more flexibility to Mars to make it easier for other servers to use.

At the moment, we would not recommend using Mars for servers that rely on perfectly accurate stat tracking, as Mars is still in early stages and stat tracking is still being refined. 

> :warning:  Mars is currently a rolling release project. This means future updates may introduce data schema changes that require migrations. There is no guarantee migrations will be available. Hopefully, in the future, we are able to transition to a versioned release system, but for now we recommend forking Mars and manually updating/merging upstream features when necessary.

### Using

Download the latest Mars build [here](https://nightly.link/Warzone/mars/workflows/build/master/Mars.zip).

On a PGM server ([setup guide](https://github.com/PGMDev/PGM/blob/dev/docs/RUNNING.md)), add the Mars plugin JAR file to the server's `plugins/` folder. After starting the server once, Mars will generate [a config file](https://github.com/Warzone/mars/blob/master/src/main/resources/config.yml) which you need to modify to match the API (API URLs and secret token). Once the config is updated, restart the server and Mars should be working (you should see a connection message in the API console).

You can learn more about Mars' architecture [here](https://github.com/Warzone/mars/tree/master/docs/CONTRIBUTING.md).

### Caveats

- For gamemode stats to work during a match (i.e. Flags Captured, Cores Leaked), the map definition (`map.xml`) must include a `<gamemode>` tag with the map's gamemodes. This may be changed in the future, but it is advisable to use `<gamemode>` tags for data accuracy regardless.
