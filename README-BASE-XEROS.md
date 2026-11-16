# DarkRealm — Xeros 317 RSPS

A feature-rich 317-revision OSRS-style RuneScape Private Server with custom content including a dedicated Solak world event, custom maps, and custom 3D models. Based on the Xeros framework.

---

## Quick Start

Double-click `start-dev.bat`. It will:
1. Kill any stale processes on port 43594
2. Build the server
3. Build the client
4. Start the server in a new window
5. Launch the client pointed at localhost

**Requirements:**
- Java 8 JRE at `C:\Program Files\Java\jre-1.8\` (for the client)
- Java 11+ JDK on PATH (for Gradle / server)
- Internet connection on first run (downloads Gradle and the game cache ~95MB automatically)

---

## Manual Start

```bat
:: Terminal 1 — Server
cd Server
gradlew.bat run

:: Terminal 2 — Client (after server is up)
"C:\Program Files\Java\jre-1.8\bin\java.exe" -jar Client\build\libs\NotObfuscatedClient.jar
```

---

## Build Only

```bat
:: Server
cd Server && gradlew.bat compileJava

:: Client JAR
cd Client && gradlew.bat createStandardJar
```

---

## Architecture

| Component | Language | Build | Entry point |
|---|---|---|---|
| Server | Java + Kotlin | Gradle 6.1.1 | `io.xeros.Server` |
| Client | Java | Gradle 6.0 | `com.client.Client` |

**Server port:** 43594 (default)  
**Data folder:** `Server/etc/` (drop tables, NPC defs, shop defs, etc.)  
**Player saves:** `Server/etc/saves/` (JSON, autosaved every 15 min)  
**Game cache:** Downloaded on first client launch to `%USERPROFILE%\.darkrealm_v1\`

---

## Content

### Skills
Agility, Cooking, Crafting, Farming, Firemaking, Fishing, Fletching, Herblore, Hunter, Mining, Prayer, Runecrafting, Slayer, Smithing, Thieving, Woodcutting

### Bosses
Vorkath, Nex, Cerberus, Kraken, Zulrah, Hydra, Nightmare, Corrupted Hunllef (Gauntlet), Commander Zilyana, General Graardor, K'ril Tsutsaroth, Kree'arra, Sarachnis, Skotizo, Bryophyta, Obor, Scorpia, Lizardman Shaman, Tekton, Sea Snake, Mimic

### Minigames & Raids
Inferno, Fight Cave, Theatre of Blood, Chambers of Xeric, Raids on Titania, Barrows, Pest Control, Last Man Standing, Warriors Guild, Fight Pits, Bounty Hunter, PK Arena, Corrupted Gauntlet

### Custom Content
- **Solak World Event** — custom map region, custom 3D model, world broadcast on spawn
- 35 custom map regions total
- 25+ custom NPC and item models
- Donation zone, legendary zone, skilling island, FOE dungeon
- Collection Log, Achievements & Diaries, Prestige system, Lottery
- Skillcape perks, Pet collector, Trading Post, Leaderboards

---

## Configuration

`Server/config.yaml` is auto-generated on first run with `server_state: DEBUG`. In DEBUG mode no MySQL is required — an embedded database is used automatically.

To use MySQL, add to `config.yaml`:
```yaml
server_state: LIVE
local_database:
  host: localhost
  port: 3306
  name: darkrealm
  user: root
  password: yourpassword
```

---

## Dev Commands (in-game chat)

| Command | Effect |
|---|---|
| `::tele x y z` | Teleport to coordinates |
| `::pos` | Print current position |
| `::give <id> [qty]` | Add item to inventory |
| `::master` | Max all skills |
| `::noclip` | Toggle noclip |
| `::bank` | Open bank |

---

## Notes

- Client must be run with **Java 8** (`jre-1.8`) — older XStream version is not compatible with Java 9+ module system
- Server runs fine on Java 11+
- Discord integration requires setting the `DISCORD_BOT_TOKEN` environment variable
- `LOCAL_HOST = true` is set in `Client/src/main/java/com/client/Configuration.java` — change to `false` for a public deployment
