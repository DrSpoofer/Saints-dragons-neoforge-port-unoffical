# Saint's Dragons — NeoForge 1.21.1 port (unofficial)

This is an **unofficial** port of the original Saint's Dragons mod, updated for **Minecraft 1.21.1** using the **NeoForge** mod loader.

It is based on Saint's Dragons **0.9.76** for Minecraft 1.20.1. The aim is to behave the same as the original, not to redesign it.

## Credits & links

- **Original creator:** SaintVanWinkle (Leon Saint) ~ https://modrinth.com/user/SaintVanWinkle
- **Original mod page:** Saint's Dragons on CurseForge ~ https://www.curseforge.com/minecraft/mc-mods/saints-dragons
- **Official wiki / support:** Saint's Dragons Compendium ~ https://raevyx.miraheze.org/wiki/Main_Page
- **Third-party art** used by the original mod is credited in [CREDIT.md](CREDIT.md).

For bugs specific to this 1.21.1 NeoForge port, please report them here on this page, **not** to the original mod team.

## Status

Current version: **0.9.76-port.4**. This is a **test build**, not a finished release.

Tested in a development environment with only Minecraft, NeoForge, GeckoLib and Saint's Dragons installed:

- The mod builds, the dedicated server starts, and singleplayer worlds load, save and reload.
- Every Saint's Dragons mob can be spawned and rendered.
- The creative and survival inventories and the Draconic Codex open.
- Dragons can be mounted, flown and steered.
- The rider camera works, including per-dragon distances, the config screen and the Raevyx lightning-beam camera.
- The rider stays on the saddle during fast turns.

It has also been loaded alongside Sodium 0.8.13 with Iris 1.8.14-beta.1, Distant Horizons, Create, Sable and Create Aeronautics without problems. Iris 1.8.12 does not work with Sodium 0.8.x; that is an Iris/Sodium version mismatch, not a problem with this mod.

**Not fully verified:** a full playthrough, multiplayer, and every dragon's abilities. Please report anything that behaves differently from the original.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 or newer in the 21.1 series (built against 21.1.251) |
| GeckoLib | 4.9.3 or newer (below 5) |
| Java | 21 |

JEI, EMI and Jade integrations are optional, as in the original.

## Building from source

You need a Java 21 JDK. The Gradle wrapper downloads everything else.

```sh
./gradlew build         # Windows: gradlew.bat build
```

The mod jar is written to `neoforge/build/libs/`.

For development:

```sh
./gradlew runClient     # development client
./gradlew runServer     # dedicated server (accept the EULA in run/server/eula.txt first)
```

## Project layout

- `common/src/main/java`: most of the mod (dragons, abilities, AI, rendering, camera, UI).
- `forge/src/main/java`: loader-specific code (events, mixins, networking, config screens), ported from Forge to NeoForge.
- `common/src/main/resources`, `forge/src/main/resources`: assets and data.
- `neoforge/`: the NeoForge build that compiles `common` and `forge` into one jar.
- `fabric/`: the original Fabric sources, kept for reference. They are **not** built.

## License

Saint's Dragons is dual-licensed by its original author; see [LICENSE.md](LICENSE.md).

- **Source code:** MIT License, Copyright (c) 2025 Leon Saint. The changes made for this port are also released under the MIT License (see [LICENSE](LICENSE)).
- **Art and audio assets** (textures, models, animations, sounds): **All Rights Reserved** by Leon Saint, and **not** covered by the MIT License. They may only be used as part of Saint's Dragons, as described in [LICENSE.md](LICENSE.md).
- **Third-party assets** remain under their own licences; see [CREDIT.md](CREDIT.md).
