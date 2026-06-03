package io.github.ktwinx.core.execution

import io.github.ktwinx.core.hdt.HumanDigitalTwin

interface App {
    fun addDt(hdt: HumanDigitalTwin): Result<String>
    fun addDts(hdts: List<HumanDigitalTwin>): List<Result<String>> {
        return hdts.map { addDt(it) }
    }
    fun addStart(hdt: HumanDigitalTwin): Result<String> {
        return addDt(hdt).onSuccess { startDt(it) }
    }
    fun addStartAll(hdts: List<HumanDigitalTwin>): List<Result<String>> {
        return hdts.map { addStart(it) }
    }
    fun removeDtById(id: String): Result<Unit>
    fun removeDtsById(ids: List<String>): List<Result<Unit>> {
        return ids.map { removeDtById(it)  }
    }
    fun removeDt(hdt: HumanDigitalTwin): Result<Unit> {
        return removeDtById(hdt.hdtId.id)
    }
    fun removeDts(hdts: List<HumanDigitalTwin>): List<Result<Unit>> {
        return removeDtsById(hdts.map { it.hdtId.id })
    }
    fun startDt(id: String): Result<Unit>
    fun startDts(ids: List<String>): List<Result<Unit>> {
        return ids.map { startDt(it) }
    }
    fun stopDt(id: String): Result<Unit>
    fun stopDts(ids: List<String>): List<Result<Unit>> {
        return ids.map { stopDt(it) }
    }
    fun startAll(): Result<Unit>
    fun stopAll(): Result<Unit>
}