# CinderAC

An anticheat for Minecraft servers that reproduces the detection behavior of a
commercial anticheat. The code is written from scratch.

- **Servers:** Paper, Purpur and Folia, 1.8.8 and newer
- **Requires:** [packetevents](https://github.com/retrooper/packetevents) 2.13.0+
- **Java:** compiled to Java 8 bytecode, so it runs on old and new servers alike

> ⚠️ **Early stage.** It has not been tested on a live server yet. Punishments are
> off by default (`settings.punishments-enabled: false`). Watch the alerts and
> tune the thresholds before you turn them on.

## Checks

179 checks in three categories:

| Category | Checks |
|----------|--------|
| Combat   | Aim, AutoBlock, AutoClicker, Criticals, FastBow, Hitbox, KillAura, Reach, Velocity |
| Movement | AntiLevitation, BoatFly, Elytra, EntityFlight, EntitySpeed, FastClimb, Flight, Jesus, Jump, Motion, NoSaddle, NoSlow, Speed, Sprint, Step, Strafe, VClip, WallClimb |
| Player   | AirPlace, Baritone, BadPackets, FastBreak, FastPlace, GhostHand, GroundSpoof, Improbable, Invalid, Scaffold, Timer, Tower |

You can configure each check in `checks.yml`: enable or disable it, set the
buffer, the maximum violations and the punishment commands.

## Installation

1. Install packetevents.
2. Put `CinderAC-x.y.z.jar` in `plugins/`.
3. Restart the server and edit `plugins/CinderAC/config.yml` and `checks.yml`.

## Commands

`/cinderac` (alias `/cac`): `alerts`, `reload`, `reset`, `violations`, `profile`,
`disablecheck`, `clickalert`

Permissions: `cinderac.alerts`, `cinderac.command`, `cinderac.bypass`
(`cinderac.bypass.setback.<check>` lets a player skip setbacks for that check)

## Building

    ./gradlew build

The jar ends up in `build/libs/`. Building needs JDK 21+, but the output targets Java 8.

## License

[Apache License 2.0](LICENSE) © airr_
