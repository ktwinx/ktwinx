package io.github.ktwinx.wldt.plugin.execution

import io.github.ktwinx.wldt.plugin.factory.HumanDigitalTwinFactory.fromHumanDigitalTwin
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.execution.App
import it.wldt.core.engine.DigitalTwinEngine

class WldtApp: App {
    private val dtEngine: DigitalTwinEngine = DigitalTwinEngine()

    override fun addDt(hdt: HumanDigitalTwin): Result<String> {
        return runCatching {
            dtEngine.addDigitalTwin(
                _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.HumanDigitalTwinFactory.fromHumanDigitalTwin(
                    hdt
                )
            )
            hdt.hdtId.id
        }
    }

    override fun removeDtById(id: String): Result<Unit> {
        return runCatching { dtEngine.removeDigitalTwin(id) }
    }

    override fun startDt(id: String): Result<Unit> {
        return runCatching { dtEngine.startDigitalTwin(id) }
    }

    override fun stopDt(id: String): Result<Unit> {
        return runCatching { dtEngine.stopDigitalTwin(id) }
    }

    override fun startAll(): Result<Unit> {
        return runCatching { dtEngine.startAll() }
    }

    override fun stopAll(): Result<Unit> {
        return runCatching { dtEngine.stopAll() }
    }
}