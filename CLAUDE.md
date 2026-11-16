# CLAUDE.md — DarkRealmMirror (Xeros 317 RSPS)

## What this is

A 317-revision OSRS-style RuneScape Private Server originally known as "Koranse". Based on the **Xeros** framework. Java server + custom 317 client. Includes custom Solak content (world event boss, custom map, custom 3D model).

Last upstream commit: April 2024. This is an abandoned project — all development is on you.

## Project layout

```
Server/          Java game server (Gradle)
Client/          Custom 317 Java client (Gradle, ProGuard obfuscation)
Data/            Game data files
  custom_maps/   35 custom map regions (.gz compressed)
  custom_models/ 25+ custom NPC/item models (.gz compressed)
```

## Commands

```bat
:: Build + run everything (recommended)
start-dev.bat

:: Build server only
cd Server && gradlew.bat compileJava

:: Run server only
cd Server && gradlew.bat run

:: Build client JAR
cd Client && gradlew.bat createStandardJar

:: Run client (must use Java 8 — Java 9+ breaks XStream)
"C:\Program Files\Java\jre-1.8\bin\java.exe" -jar Client\build\libs\NotObfuscatedClient.jar
```

## Architecture

**Language:** Java 8 (server + client), small amount of Kotlin  
**Network:** Netty 4.x  
**Database:** MySQL 8 (primary) with embedded DB fallback — works without MySQL  
**Connection pooling:** HikariCP  
**DB migrations:** Flyway  
**Build:** Gradle 6.x with wrapper  
**Main class:** `io.xeros.Server`

### Java version requirements
- **Server:** Java 11+ (tested on Java 11.0.29 Corretto)
- **Client:** Must use **Java 8** (`C:\Program Files\Java\jre-1.8\bin\java.exe`)
  - XStream 1.4.7 uses `Sun14ReflectionProvider` which is incompatible with Java 9+ module encapsulation
  - Running with Java 9+ causes `InaccessibleObjectException` in `ItemBonusDefinitionLoader` and the client hangs at "Loading title screen - 0%"
  - JVM version check in `JVMVersionChecker.java` has been disabled for local dev
- **`LOCAL_HOST = true`** is set in `Client/src/main/java/com/client/Configuration.java` — flip to `false` for live deployment

### Server package structure (`Server/src/io/xeros/`)

| Package | Purpose |
|---|---|
| `bosses/` | Boss implementations (Nex, Vorkath, Cerberus, Hunllef, etc.) |
| `combat/` | Combat mechanics, hit calculations |
| `commands/` | In-game command system |
| `content/` | Skills, minigames, world events, items |
| `content/world_event_solak/` | Solak world event boss |
| `database/` | DB query abstractions |
| `model/entity/player/` | Player entity |
| `model/entity/npc/` | NPC entity |
| `net/` | Netty networking |
| `sql/` | HikariCP + Flyway setup |
| `task/` | Tick/cycle event system |
| `util/` | Discord integration, logging, misc |

### Configuration

Server reads `config.yaml` from the working directory. Set `server_state` to `DEBUG`, `TEST`, or `LIVE`. Database credentials are also in `config.yaml`.

```yaml
server_state: DEBUG
local_database:
  host: localhost
  port: 3306
  name: xeros
  user: root
  password: yourpassword
```

If no MySQL is available, the embedded DB fallback kicks in automatically.

## What's implemented

### Skills
Agility, Cooking, Crafting, Farming, Firemaking, Fishing, Fletching, Herblore, Hunter, Mining, Prayer, Runecrafting, Slayer, Smithing, Thieving, Woodcutting

### Bosses
Vorkath, Nex (multi-phase), Cerberus, Kraken, Zulrah, Hydra, Nightmare, Hunllef (Corrupted Gauntlet), Commander Zilyana, General Graardo, Kril Tsutsaroth, Kree'arra, Sarachnis, Skotizo, Tekton, Bryophyta, Obor, Scorpia, Sea Snake, Mimic, Lizardman Shaman

### Minigames & raids
Inferno, Fight Cave, Theatre of Blood (ToB), Chambers of Xeric (CoX), Barrows, Pest Control, Last Man Standing, Warriors Guild, Fight Pits, Bounty Hunter, PK Arena, Raids on Titania (ToA-style)

### World events
Solak (custom), Galvek, Evil Tree, Shooting Star, Hespori, Revenants, Koranian event

### Other systems
Collection Log, Achievements & Diaries, Prestige, Trading Post, Lottery, Donation system, Pet collector, Skillcape perks, Leaderboards

## Solak specifics

Solak is implemented as a **world event boss** (not a complex raid):
- NPC ID: 3407, Key ID: 22428
- Stats: HP 3000, Max Hit 50, Attack 650, Defence 450
- Spawn: `Boundary.SOLAK_AREA` at ~(1718, 3882)
- Has a dedicated custom map region (`Data/custom_maps/solak_area/`)
- Has a custom 3D model (`Data/custom_models/solak/`)
- Discord announcement integration on spawn
- To add multi-phase mechanics: extend `SolakEventBossHandler.java`

## Adding content

Register handlers in `Server/src/io/xeros/content/` following the existing pattern. For boss scripts look at any file under `bosses/` — they all extend a common base and override attack/death/spawn methods.

## Known issues / things to watch

- Dependencies are outdated (last updated 2021–2022). Consider upgrading Netty and Jackson if you hit issues.
- Java 8 target — if you upgrade to Java 11+ check for removed APIs.
- Client uses ProGuard obfuscation on build — keep `proguard.conf` up to date if you add new classes.
- Player saves use a hybrid file+DB approach with 15-minute autosave and 7-day backup rotation.
- No active upstream — you own all maintenance.
