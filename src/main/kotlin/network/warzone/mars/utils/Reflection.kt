package network.warzone.mars.utils

import network.warzone.mars.feature.resource.Resource
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

data class Difference(val name: String, val source: String, val new: String)

inline fun <reified T : Resource> T.diff(other: T, ref: KClass<T>): List<Difference> {
    return ref.memberProperties
        .filter { it.get(this) != it.get(other) }
        .map { Difference(it.name, it.get(this).toString(), it.get(other).toString()) }
}