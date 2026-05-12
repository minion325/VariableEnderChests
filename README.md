# VariableEnderChests — Folia Fork

Unofficial Folia-compatible fork of [VariableEnderChests](https://www.spigotmc.org/resources/variableenderchests-1-8-1-21-10-%E2%9A%A1asynchronous%E2%9A%A1%E2%9C%85no-dupes%E2%9C%85.102187/) by **minion325**.
Published with the original author's permission.

- Upstream Spigot resource: <https://www.spigotmc.org/resources/.102187/>
- Upstream GitHub: <https://github.com/minion325/VariableEnderChests>

## What's different in this fork

Folia 26.1.2 (Minecraft 1.21.x) support. All Bukkit scheduler calls were routed through a reflection-based `FoliaScheduler` abstraction that picks the right scheduler at runtime:

| Original call                             | Folia routing                      |
|-------------------------------------------|------------------------------------|
| `Bukkit.getScheduler().runTask`           | `GlobalRegionScheduler.run`        |
| `Bukkit.getScheduler().runTaskLater`      | `GlobalRegionScheduler.runDelayed` |
| `scheduleSyncRepeatingTask`               | `GlobalRegionScheduler.runAtFixedRate` |
| `runTaskAsynchronously`                   | `AsyncScheduler.runNow`            |
| Per-player ops (close inv, reopen, title) | `EntityScheduler.run`              |

On non-Folia servers (Paper / Spigot) the abstraction falls through to the normal Bukkit scheduler, so the same jar still works on regular servers.

Also in this fork:
- `plugin.yml` now declares `folia-supported: true`.
- `pom.xml` switched from Spigot 1.19.4 to `dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT` (publicly resolvable from papermc.io). Java 17.
- `ChestSortHook` rewritten to load ChestSortAPI via reflection (so the plugin compiles without the ChestSortAPI jar; the hook still works at runtime if ChestSort is installed).
- `ReflectionUtils` vendored into the project (jitpack jar was unavailable).
- Two stray unused imports removed (`org.omg.CORBA.PUBLIC_MEMBER`, `net.minecraft.network.chat.contents.NbtContents`).

No gameplay behavior was changed.

## Building

```bash
mvn clean package
```

Output: `target/VariableEnderChests-1.11.7.0.jar`. Drop it into your Folia (or Paper / Spigot) server's `plugins/` folder.

## Credit

All design and gameplay logic belongs to **minion325**. This fork only adapts the threading model for Folia and adjusts build dependencies. If you find this plugin useful, please support the original author on the [Spigot resource page](https://www.spigotmc.org/resources/.102187/).

## License

Released under the terms granted by the original author (see `NOTICE.md`). No claim is made over the original work.
