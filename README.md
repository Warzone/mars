# Mars

Mars is an all-in-one plugin for [PGM](https://github.com/PGMDev/PGM) servers.<img src="https://warzone.network/img/warzone.svg" align="right"
     alt="Warzone Logo" width="120" height="178" style="margin-top: -5%">

- **[Using Mars](https://github.com/Warzone/mars/tree/master/docs/USING.md)**
- **[Contributing to Mars](https://github.com/Warzone/mars/tree/master/docs/CONTRIBUTING.md)**

## Why?

Mars was built to handle everything that PGM isn't supposed to handle. Aside from extensive tracking of player, match, and map stats, Mars' feature set includes comprehensive community moderation tools, permission groups (ranks), other optional entitlements (such as chat tags), and more. 

PGM does provide a few of these features out of the box (stat tracking is not persistent), and an official [Community](https://github.com/PGMDev/Community) plugin is in development, but Mars is designed to integrate with other platforms (i.e. web, Discord), and is somewhat opinionated.

## Do I need Mars?

There's a good chance you don't.

If you simply want to host a game server running PGM, whether it's competitive, casual, or arcade, PGM as a standalone plugin is probably good enough.

**You'd benefit from Mars if you want...**

- Comprehensive moderation tools beyond those included in PGM/Community
- Leaderboards (periods spanning [from daily to all-time](https://github.com/Warzone/mars-api/blob/master/src/main/kotlin/socket/leaderboard/Leaderboard.kt#L34)) for [a variety of metrics](https://github.com/Warzone/mars-api/blob/master/src/main/kotlin/socket/leaderboard/Leaderboard.kt#L88)
- Player, match, and map statistics tracked
- Automatic chat broadcasts
- Permission groups (ranks) that can be customised and assigned in-game
- Chat suffixes (tags) that can be customised and assigned in-game
- An XP/levelling progression system that rewards team contributions

Mars also overrides and extends some default PGM behaviours. Two examples of this are Mars overriding PGM's player preference system, and Mars announcing enemy objective advancements in chat (PGM intentionally doesn't do this).

## Support

Please raise any questions or issues on the [Issues page](https://github.com/Warzone/mars/issues) or the [Warzone Discord server](https://warz.one/discord). We are happy to help anyone looking to use Mars for their server or submit contributions to the Mars project.
