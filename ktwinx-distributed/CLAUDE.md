# CLAUDE.md — ktwinx-distributed

## Purpose

Serialization and MQTT messaging contracts for distributed HDT deployments. Exists as a separate module so the runtime adapter can depend on wire formats without pulling serialization logic into the pure domain model.

## Public API

- `SerDe<T>` — symmetric JSON encode/decode interface; `jsonSerDe<T>(json)` factory builds one from any `Json` instance
- `Stub` — pre-built `SerDe` factory for core types: `hdtJsonSerDe()`, `propertyJsonSerDe()`, `modelJsonSerDe()`, `physicalInterfaceJsonSerDe()`, `digitalInterfaceJsonSerDe()`, `messageJsonSerDe()`
- `Message` — distributed event envelope with `HdtId`, `SenderId`, timestamps, and a `JsonElement` payload; `message.unwrap<T>()` decodes the payload
- `Namespace` — MQTT topic constants and builders (`propertyUpdateRequestTopic`, `propertyUpdateNotificationTopic`)
- `SenderId` — inline value class for sender identity; prefer `SenderId.of(raw)` over the direct constructor (validates non-empty, ≤64 chars)

## Depends on

- `ktwinx-core` (`implementation`, not `api`) — provides domain types as SerDe targets; consumers must declare their own `ktwinx-core` dependency

## Consumed by

- `ktwinx-wldt-plugin` — uses `Stub` SerDe instances and `Namespace` for MQTT adapter wiring

## Package map

- `serde` — `SerDe<T>` interface, `jsonSerDe()` factory, `Stub` object
- `serde.modules` — `SerializersModule` definitions for `PhysicalInterface`, `DigitalInterface`, and `PropertyValue` polymorphic dispatch
- `message` / `id` — `Message` envelope and `SenderId`
- `namespace` — `Namespace` MQTT topic conventions

## Non-obvious constraints

- `Message.unwrap<T>()` decodes using the bare default `Json` instance with no polymorphic modules. Calling `message.unwrap<Property>()` will fail at runtime because `PropertyValue` subtypes aren't registered; use `Stub.propertyJsonSerDe().deserializeFromJsonElement(message.payload)` instead.
- `Message.receivedAt` is `@Transient` — always reset to `Clock.System.now()` on deserialization. Do not use it for cross-process message ordering.