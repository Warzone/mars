## Using Mars

### Before you use...

Mars was specifically built for use on [Warzone](https://warzone.network), so some components of Mars may be harder to configure or may not be as applicable to other communities with different needs. That being said, we are more than happy to consider Pull Requests adding more flexibility to Mars to make it easier for other servers to use.

At the moment, we would not recommend using Mars for servers that rely on perfectly accurate stat tracking, as Mars is still in early stages and stat tracking is still being refined. 

> :warning:  Mars is currently a rolling release project. This means future updates may introduce data schema changes that require migrations. There is no guarantee migrations will be available. Hopefully, in the future, we are able to transition to a versioned release system, but for now we recommend forking Mars and manually updating/merging upstream features when necessary.

### Using

1. Download the latest Mars build. You can find this by going to the [actions tab](https://github.com/Warzone/mars/actions) and navigating to the latest successful build.

2. Download the latest [Mars API](https://github.com/Warzone/mars-api) build. You can find this by going to the [actions tab](https://github.com/Warzone/mars-api/actions) and navigating to the latest successful build.

3. Ensure you have a cloud database set up. For those unfamiliar with database creation, we recommend using MongoDB [(installation guide)](https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-windows/).

4. Ensure [Redis](https://redis.io/download/#redis-downloads) is installed and running. For Windows users, you can follow [this tutorial](https://redis.io/docs/getting-started/installation/install-redis-on-windows/) for setting up Redis using WSL2. 

5. In a terminal, open the directory with the standalone Mars API jar, and run it. If you're using a MongoDB localhost with the default port, you won't need to change anything; otherwise, you will need to modify the API URLs accordingly. By default, Mars API runs on port 3000.
```bash
cd /path/to/folder
java -jar Mars-API.jar
```
6. On a PGM server ([setup guide](https://github.com/PGMDev/PGM/blob/dev/docs/RUNNING.md)), add the Mars plugin JAR file to the server's `plugins/` folder. After starting the server once, Mars will throw an error and generate [a config file](https://github.com/Warzone/mars/blob/master/src/main/resources/config.yml), which you need to modify to match the API (API URLs and secret token). Once the config is updated, restart the server and Mars should be working (you should see a connection message in the API console).

(Note: If you are having connection timeout issues, try turning off your anti-virus firewall).

You can learn more about Mars' architecture [here](https://github.com/Warzone/mars/tree/master/docs/CONTRIBUTING.md).

### Sample Config File

```bash
server:
  id: 'main'
  links:
    appeal: 'https://warz.one/discord'
    store: 'https://warz.one/store'
    discord: 'https://warz.one/discord'
    rules: 'https://warz.one/rules'

api:
  secret: 'auth'                  # modified api token
  socket:
    url: 'wss://localhost:3000'   # modified api url
  http:
    url: 'http://127.0.0.1:3000'  # modified api url

chat:
  enabled: true
```

### Caveats

- For gamemode stats to work during a match (i.e. Flags Captured, Cores Leaked), the map definition (`map.xml`) must include a `<gamemode>` tag with the map's gamemodes. This may be changed in the future, but it is advisable to use `<gamemode>` tags for data accuracy regardless.
