# CLAUDE.md — ktwinx-examples

## Purpose

Runnable demonstrations of the full ktwinx stack. Not a library — not published to GitHub Packages (no `maven-publish` plugin, hardcoded `version = "0.0.0-SNAPSHOT"`).

## Depends on

- `ktwinx-core` — constructs HDT domain descriptors directly
- `ktwinx-wldt-plugin` — runs them via `WldtApp`

## Consumed by

None — leaf node in the module graph.

## Package map

Single file: `Main.kt` at `io.github.ktwinx`. No sub-packages.

## Non-obvious constraints

- `Property` requires a `ModelId` at construction time, but `Model.id` is a computed field not available until the `Model` is built. `Main.kt` works around this by manually constructing `ModelId("$hdtId:$modelName")` ahead of time. If the model name changes, the pre-constructed `ModelId` must be updated separately or property IDs will silently diverge from the model's own `id`.
- MQTT broker defaults to `localhost:1883`; HTTP defaults to `localhost:8080`. The example requires a running MQTT broker to do anything useful — it will start but immediately fail to connect otherwise.

## Test conventions

No tests exist in this module.