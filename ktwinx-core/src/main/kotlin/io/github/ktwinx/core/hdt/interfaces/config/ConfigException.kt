package io.github.ktwinx.core.hdt.interfaces.config

sealed class ConfigException(message: String) : RuntimeException(message)

class MissingConfigKeyException(key: String, interfaceType: String) :
    ConfigException("missing required config key '$key' for interface type $interfaceType")

class MalformedConfigValueException(key: String, expectedType: String, actual: String?) :
    ConfigException("config key '$key' expected $expectedType, got '$actual'")
