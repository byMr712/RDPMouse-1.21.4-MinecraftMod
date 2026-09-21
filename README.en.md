# RDPMouse (Fabric 1.21.4)
> **Language:** [Русский](README.md) · English

## Fork Information
All rights belong to the original developer — [KesslerCascade](https://github.com/KesslerCascade/RDPMouse).

Patch and port for **Minecraft 1.21.4**:
- Ported to **Minecraft 1.21.4** for **Fabric** (Java 21, Fabric Loom 1.10.1).
- Adapted key mapping registration and GLFW Window / Mouse hooks for 1.21.4 Yarn mappings.
- Decoupled into a clean standalone Fabric project (fast build times, zero redundant layers).
- Added full Russian (u_ru) and English (n_us) localization.
- Added uild.bat helper script.

Support: **Fabric 1.21.4**.

## Overview
Minecraft uses raw relative mouse input, which Windows blocks over Remote Desktop (RDP). The result is a camera that spins uncontrollably the moment you try to look around ([MC-107122](https://bugs.mojang.com/browse/MC-107122) and [MC-126875](https://bugs.mojang.com/browse/MC-126875)).

RDPMouse replaces raw mouse input with absolute cursor position tracking, which RDP supports, making the game fully playable over Remote Desktop.

## Usage
- Press **F8** to toggle RDP Mode on or off.
- When RDP Mode is active, moving the mouse controls the camera normally within window boundaries.
- If the camera stops turning, the cursor has reached the edge of the window: hold **Alt** to release the cursor, recenter it, and release Alt to resume.
- **Arrow keys** can also be used to turn the camera from the keyboard.

## Key Bindings

| Key | Action |
|---|---|
| F8 | Toggle RDP Mode |
| Alt (hold) | Release cursor to recenter |
| Arrow keys (Left/Right/Up/Down) | Pan camera |

All bindings can be customized in Options -> Controls -> Key Binds -> RDP Mouse.

## Building
To build the mod, run:
`at
build.bat
`
or via Gradle:
`ash
./gradlew build
`
Built jar file is located in uild/libs/RDPMouse-1.21.4-byMr712.jar.
