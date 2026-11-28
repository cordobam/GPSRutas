package com.example.melirutas.reparto

data class ZonaApp(
    val id: String,
    val nombre: String
)

data class ZonaReparto(
    val id: String,
    val nombre: String,
    val zonaAppId: String? = null,
    val rangos: List<ReglaRango> = emptyList(),
    val trackIds: List<Long> = emptyList()
)

data class ReglaRango(
    val desde: Long,
    val hasta: Long
)

data class RepartoDia(
    val fecha: String,
    val zonas: List<ZonaReparto> = emptyList()
)
