# Hachimi 2.0

A PvP/anarchy client for Fabric 1.21.1 — combat modules, exploits, rendering, movement and a ClickGUI.

This repository holds the **client sources** (`src/main/java`), remapped from intermediary (`class_1234`,
`field_1234`, `method_1234`) to Yarn 1.21.1 names, a built jar, and the original archive
`Hachimi-2.0.jar_Decompiler.com.zip`.

---

## Current errors
- the built jar is outdated and not usable — build from source (`./gradlew build`, see [Building](#building))
- fixed: GUI sounds (wrong namespace in `sounds.json`) and font resizing / custom font loading (`Fonts`)

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.15.0 or newer |
| Fabric API | any build for 1.21.1 |
| Java | 21 |

## Installation

1. Install Fabric Loader for 1.21.1.
2. Drop `fabric-api` and `Hachimi-2.0-built.jar` from this repo into `.minecraft/mods/`.
3. Launch the game — the client loads as a mod (`environment: client`).

> The jar is built from these sources but has **not been launched in game** — treat it as a build to test.

## Controls

- **ClickGUI** — bound to the `ClickGui` module key by default (Client category, rebindable).
  - left click a module — toggle, right click — settings, **middle click — show/hide in the arraylist**
  - right click a panel header — collapse, drag the header — move (panels stay on screen)
  - **Ctrl+F** — focus search; Enter keeps the filter, Esc clears it, right click on the box clears it
  - scroll scrolls the panel under the cursor (hold **Shift** to scroll all panels)
- **Commands** — prefix `.` (e.g. `.help`). The prefix and its key are changed with `.prefix`.
- In game, `.help` prints the current command list with descriptions.

---

## What the GUI looks like

![Hachimi ClickGUI](docs/clickgui.png)

Seven draggable panels, one per category, laid out left to right from `(15, 15)` with `2 × scale` pixels between
them, over a screen dimmed to `#0D1017` at 73% alpha. Each panel has a `#0D1017` header at 93% alpha, a `#101418`
body at 87% alpha (with optional blur), a 2 px accent bar on the left edge in the client colour, and a 1 px divider
in the accent colour at 80 alpha under the header.

Module rows are 103 × 13 px: white text when the module is on, grey when off, and the row tinted in the accent
colour while enabled. Expanding a module reveals its config rows — checkbox (`BooleanConfig`), slider
(`NumberConfig`), dropdown (`EnumConfig`), text field (`StringConfig`), colour picker (`ColorConfig`) and keybind.
Hovering any row shows its description in a black tooltip next to the cursor, offset `+10 / -8` px. The Client panel
additionally has a search box at the top.

The image above is generated from the layout constants by `docs/render_clickgui.py` — it is not a screenshot, since
the client has not been run here:

```bash
python3 docs/render_clickgui.py    # -> docs/clickgui.png (needs Pillow)
```

### HUD

The HUD is a separate overlay drawn in game (`HUDModule`): the **Hachimi 2.0** watermark in the top left, the
animated module arraylist down the right edge, and info lines in the bottom left. Potion timers are a separate
stack near the centre-right, armour and totems sit with the info lines, and every element is a toggle:

| Element | Default | Element | Default |
|---|---|---|---|
| Watermark (`Hachimi 2.0`) | on | Coordinates + nether coords | on |
| Logo (top left, scale/opacity) | off | Server brand | off |
| Server status warning | on | Chests in render distance | off |
| User info | on | Speed (`km/h`, `m/s`, blocks) | on |
| Facing direction (+ yaw/pitch) | on / off | Ping | on |
| Armor (+ durability) | on / off | TPS | on |
| Totems | off | Packets per second | off |
| Potion effects (+ timers) | on | FPS | on |
| Held item durability | off | Arraylist (ordering by length) | on |

The arraylist animates over up to 2000 ms, can be ordered and rendered upward or downward, and supports rainbow
modes. Vanilla potion HUD and item-name display can each be left alone, hidden or replaced.

### Default palette

Taken from the colour constants in the code (`ClickGuiScreen`, `CategoryFrame`, `ColorsModule`):

| Role | Value |
|---|---|
| Screen dim behind the GUI | `#0D1017` at 73% alpha |
| Panel background | `#0D1017` at 93% alpha |
| Panel body / module list | `#101418` at 87% alpha |
| Accent (bars, dividers, enabled rows) | `#823CDC` (`Colors` module, default purple) |
| Text | `#FFFFFF`, grey `#AAAAAA` for disabled modules |
| Description tooltip | `#000000` at 20–43% alpha |

### Sizes and behaviour

| | |
|---|---|
| Panel width / header height | 105 px / 15 px |
| Module row | 103 × 13 px |
| Panel spacing | `2 × scale` px |
| GUI scale | 0.5 – 3.0, default 1.0 |
| Open / close animation | 200 ms, cubic in-out (frame), linear (overlay) |
| Scroll speed | 5 – 100, default 30 |
| Description box | offset `+10 / -8` px from the cursor |

The panels are draggable by their header, clamp to the screen when scrolled, and the GUI does not pause the game
(`shouldPause()` returns `false`).

---

## Modules

### Combat
Aura, AutoCrystal, PistonCrystal, AutoAnchor, Surround, AutoTrap, AutoCrawlTrap, AntiCrawl, SelfTrap, SelfFill,
AutoTotem, AutoArmor, AutoPotion, AutoRelease, AutoWeb, AutoXP, AutoMine, AutoRegear, AntiRegear, AntiWeakness,
Criticals, MaceAura, KeyPearl, Offhand, Mainhand, NoHitDelay, Blocker, BasePlace, PistonKick, SpearKill, SelfBow, AutoLog

### Exploits
PacketFly, Phase, Disabler, Backtrack, FakeLatency, FastLatency, FastProjectile, GodMode, ChorusControl,
ChorusInvincibility, XCarry, InventorySync, MaceSpoof, NewChunks, NoMineAnimation, PacketCanceler, RocketExtend, Crasher, AntiHunger

### Movement
Speed, Flight, ElytraFly, BoatFly, TridentFly, FakeFly, Velocity, Step, LongJump, Parkour, SafeWalk, Jesus, IceSpeed,
NoSlow, NoFall, NoJumpDelay, NoAccel, FastFall, FastSwim, Sprint, AutoWalk, EntitySpeed, EntityControl, Follower,
FireworkBoost, TickShift, FakeLag, AntiLevitation, Yaw

### Render
ESP, Nametags, Chams, Tracers, HoleESP, PhaseESP, StorageESP, BreakESP, Search, Skeleton, Trajectories, Waypoints,
Breadcrumbs, BlockHighlight, Crosshair, ViewModel, Animations, Ambience, Skybox, Shaders, Particles, KillEffects,
Fullbright, NoRender, NoWeather, NoBob, MotionCamera, Zoom, NameProtect, Tooltips, CrystalModel, TrueSight, ViewClip, PlaceEcho

### Player
Scaffold, AirPlace, AutoTool, AutoEat, FastEat, FastPlace, AutoFish, AutoMount, AutoRespawn, Speedmine,
InventoryManager, Replenish, Freecam, FreeLook, MultiTask, NoGlitch, NoRotate

### Miscellaneous
Timer, ChestStealer, ChestSwap, AutoLogin, AutoReconnect, AutoAFK, AntiAFK, AntiAim, AntiSpam, AutoAnvilRename,
AutoFrameDupe, BetterChat, ChatNotifier, ExtraTab, FakePlayer, Kit, MiddleClick, NoPacketKick, NoSoundLag, PMSound,
Server, Shulkerception, SkinBlink, Spammer, Swing, TrueDurability

### Client
HUD, CombatHud, ClickGui, Colors, Font, Socials, Rotations, Chat, Anticheat, RPC, **Keystrokes**, **Notifications**

---

## Changelog (ui-overhaul)

**ClickGUI**
- panel headers show `enabled/total` (or `matches/total` while searching) — `Counters`
- module rows show their keybind on the right — `Binds`
- search matches names **and descriptions**, keeps the filter after you click a module, shows the match count,
  and can hide non-matching rows instead of greying them out — `SearchMode` (Dim / Hide)
- Ctrl+F focuses the search box, Ctrl+Backspace clears it, spaces can be typed
- middle click toggles a module's arraylist visibility; the tooltip says when a module is shown in the arraylist
- description tooltip stays inside the screen (flips left at the right edge) and scales with the GUI scale
- scroll only moves the hovered panel — `ScrollHovered` (Shift = all panels)
- open/close zoom animation now scales from the screen centre (it used the height for the X pivot)
- dragged panels are clamped to the screen

**New modules (Client)**
- `Notifications` — toasts for module toggles (and `NotificationsModule.getInstance().push(...)` for anything
  else): slide animation, accent bar, lifetime bar; `Position`, `Duration`, `Max`, `YOffset`, `ReplaceChat`
  (no `[+]/[-]` chat spam while enabled)
- `Keystrokes` — W/A/S/D, LMB/RMB with CPS and space, press animation in the client colour; `X`, `Y`, `Size`

**Fixes**
- `sounds.json` pointed at the `hachimi:` namespace while the files live in `assets/hachimiclient/` — GUI sounds
  were silent
- `Fonts.setSize` opened the default font with `FileInputStream("assets/...")`, which always failed, so changing
  the font size did nothing; `closeFonts` crashed when the font failed to load

---

## Commands

Prefix `.`:

```
bind        config      connect     coords      customfont  disableall
drawn       friend      hclip       help        hideall     history
kit         leave       load        macro       modules     nbt
notify      openfolder  peek        prefix      queue       reloadsound
reset       resetgui    save        skingrab    stats       toggle
vclip       vanish      waypoint    yaw
```

`.help` prints the full list with descriptions and arguments.

---

## Repository layout

```
Hachimi-2.0-built.jar                 built client (Fabric 1.21.1, intermediary jar)
Hachimi-2.0.jar_Decompiler.com.zip    original archive (CFR decompile, intermediary namespace)
docs/clickgui.png                     ClickGUI render used above
docs/render_clickgui.py               generator for that image
src/main/java/net/hachimi/...         client sources after the Yarn 1.21.1 remap
src/main/java/meteordevelopment/...   Discord IPC library used by the RPC module
```

## State of the sources

- Remap: 775 files, 10 533 class references, 6 188 fields and 7 217 methods renamed to Yarn 1.21.1+build.3.
- **One** intermediary reference remains: `method_51531` in `MixinChunkSkylightProvider` — Yarn has no name for
  it, so it is used explicitly in `@Inject` with a comment.
- The tree compiled cleanly (Loom 1.9.2, MC 1.21.1, fabric-api 0.116.9+1.21.1) once the decompiler artifacts
  were repaired.
- Java 21, Gradle 8.14+.

## Building

The repo now has a Loom setup (`build.gradle`, wrapper, `src/main/resources` with `fabric.mod.json`,
`mixins.hachimi.json`, the access widener remapped to Yarn names, and the assets from the original jar):

```bash
./gradlew build          # -> build/libs/Hachimi-2.1.jar
./gradlew runClient      # dev client
```

Versions are in `gradle.properties` (MC 1.21.1, Yarn `1.21.1+build.3`, Loom 1.9.2, Fabric API `0.116.9+1.21.1`,
Gradle 8.14.3, Java 21). Two access-widener entries from the original jar (`method_4212`, `method_4217` on
`PlayerEntityRenderer`) don't exist in 1.21.1 and were dropped. Four mixins exist in the sources but are not listed
in `mixins.hachimi.json` (same as the original jar): `sound.MixinSoundSystem`, `world.MixinEntityView`,
`texture.MixinPlayerSkinProvider`, `accessor.AccessorUpdateBeaconC2SPacket`.

---

The sources here are the result of decompiling and remapping the original jar.
