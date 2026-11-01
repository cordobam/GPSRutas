package com.example.melirutas.cost

enum class PeajeDireccion(val etiqueta: String, val factor: Int) {
    IDA("Ida", 1),
    VUELTA("Vuelta", 1),
    IDA_VUELTA("Ida/Vuelta", 2)
}
