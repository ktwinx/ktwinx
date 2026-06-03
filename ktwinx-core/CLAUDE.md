# CLAUDE.md — ktwinx-core

## Purpose

Defines the HDT type hierarchy and serialization contracts with no runtime dependencies. Exists as a separate module so the serialization layer, WLDT adapter, and examples can all share one source of truth for core types without coupling to any execution runtime.

## Public API

- `HumanDigitalTwin` — central aggregate descriptor
- `Model` / `Property` / `PropertyValue` — data model hierarchy; `PropertyValue` is a sealed class with primitive subtypes and `.pv()` extension helpers on its companion object
- `PhysicalInterface` / `DigitalInterface` — sealed interfaces for connector descriptors; concrete impls: `MqttPhysicalInterface`, `MqttDigitalInterface`, `HttpDigitalInterface`
- `Storage` — storage descriptor; `Storage.default(hdtId)` builds an IN_MEMORY instance; `StorageType` enumerates `IN_MEMORY`, `DB_MONGO`, `DB_POSTGRESQL`
- `App` — interface defining the add/start/stop HDT lifecycle contract; not implemented in this module

## Depends on

No project module dependencies. External: `kotlinx.serialization` (via Gradle plugin).

## Consumed by

- `ktwinx-distributed` — builds `SerDe` instances over core types for MQTT messaging
- `ktwinx-wldt-plugin` — converts `HumanDigitalTwin` descriptors to runnable WLDT instances; provides the `App` implementation as `WldtApp`
- `ktwinx-examples` — constructs HDT descriptors directly

## Package map

- `hdt` — `HumanDigitalTwin` root aggregate and `HdtId`
- `hdt.model` / `hdt.model.property` — `Model` and `Property`/`PropertyValue` hierarchy
- `hdt.interfaces.{digital,physical}` — connector descriptor sealed interfaces and concrete MQTT/HTTP types
- `hdt.storage` / `execution` — `Storage` descriptor and `App` lifecycle interface

## Non-obvious constraints

- IDs (`ModelId`, `PropertyId`, etc.) are computed fields (`"$parentId:$name"`), not stored and are created via `HdtIdFactory`. Renaming entities is done via the extension functions inside the factory. Each entity has ID collision detection on init {} blocks.
- `PropertyValue` `.pv()` helpers (e.g. `"foo".pv()`) require `import io.github.ktwinx.core.hdt.model.property.PropertyValue.Companion.*` at call sites.
- `DigitalInterfaceImpl`/`PhysicalInterfaceImpl` are generic impls accepting a type enum; prefer the named concrete types (`MqttDigitalInterface`, `HttpDigitalInterface`, `MqttPhysicalInterface`).

## Test conventions

No tests exist in this module currently.

## Uncertainties
- `gson:2.13.1` is declared as `implementation` but no Gson imports appear in any source file [UNCERTAIN: may be vestigial or needed transitively at runtime] -> RESOLVED.