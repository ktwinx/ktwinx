package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration
import it.wldt.core.engine.DigitalTwin

class HttpDigitalAdapterFactory : DigitalAdapterFactory {
    override val interfaceType = DigitalInterfaceType.HTTP

    override fun validate(dI: DigitalInterface): Result<Unit> = runCatching {
        dI.optionalString("host",
            DEFAULT_HOST
        )
        dI.optionalInt("port",
            DEFAULT_PORT
        )
    }

    override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): HttpDigitalAdapter {
        val host = dI.optionalString("host",
            DEFAULT_HOST
        )
        val port = dI.optionalInt("port",
            DEFAULT_PORT
        )
        val httpConfig = HttpDigitalAdapterConfiguration(dI.id.toString(), host, port)
        httpConfig.addPropertiesFilter(models.flatMap { it.properties }.map { it.id.toString() })
        return HttpDigitalAdapter(httpConfig, dt)
    }

    private companion object {
        const val DEFAULT_HOST = "localhost"
        const val DEFAULT_PORT = 8080
    }
}
