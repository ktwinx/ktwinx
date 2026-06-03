# CLAUDE.md — ktwinx-wldt-plugin

## Purpose

Bridges the ktwinx domain model to the WLDT execution runtime. Exists as a separate module to keep `ktwinx-core` free of WLDT dependencies while still providing a ready-to-run HDT engine.

## Public API

- `WldtApp` — concrete `App` implementation; wraps a `DigitalTwinEngine`; entry point for running HDTs
- `HumanDigitalTwinFactory.fromHumanDigitalTwin(hdt)` — converts a `HumanDigitalTwin` descriptor into a runnable WLDT `DigitalTwin` by wiring adapters and storage
- `ktwinxShadowingFunction` — WLDT `ShadowingFunction` that maps HDT model properties, events, and actions into WLDT state transactions; instantiated internally by the factory, not intended for direct use

## Depends on

- `ktwinx-core` (`implementation`) — domain types consumed by factory and shadowing function
- `ktwinx-distributed` (`implementation`) — `Stub.propertyJsonSerDe()` and `Namespace` for MQTT topic/payload wiring
- `wldt-core:0.4.0`, `mqtt-physical-adapter:0.1.2`, `mqtt-digital-adapter:0.1.2`, `http-digital-adapter:0.2` — WLDT runtime and adapter libraries

Both project deps are `implementation`, not `api` — consumers must declare their own `ktwinx-core` and `ktwinx-distributed` dependencies.

## Consumed by

- `ktwinx-examples`

## Package map

- `execution` — `WldtApp` (`App` implementation backed by `DigitalTwinEngine`)
- `factory` — `HumanDigitalTwinFactory`; converts domain descriptors to WLDT objects
- `shadowing` — `ktwinxShadowingFunction`; WLDT lifecycle callbacks for state sync

## Non-obvious constraints

- `DB_MONGO` and `DB_POSTGRESQL` storage types silently fall through to `DefaultWldtStorage` (IN_MEMORY) with no warning. Non-memory storage is not implemented.
- `DigitalInterfaceImpl` and `PhysicalInterfaceImpl` (the generic impls from core) also fall through with only a `logger.warning` — they are never wired up. Only the named typed impls (`MqttPhysicalInterface`, `MqttDigitalInterface`, `HttpDigitalInterface`) are handled.
- `setupStartingModels()` in `ktwinxShadowingFunction.onCreate()` has an empty `try` body — models are not initialized on creation. Actual property/event/action registration happens in `onDigitalTwinBound()`.
- MQTT payloads are raw serialized `Property` JSON, not `Message`-wrapped. The `Message` envelope code is commented out in `HumanDigitalTwinFactory`. Physical subscribers and digital publishers must agree on this raw format.

## Test conventions

No tests exist in this module currently.