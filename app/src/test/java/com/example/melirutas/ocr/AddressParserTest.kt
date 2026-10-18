package com.example.melirutas.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del parser contra los textos OCR REALES de las 6 fotos de Imagenes_prueba,
 * generados con tesseract 4 + spa+eng + PSM 6 (el mismo modo que usa TesseractEngine).
 */
class AddressParserTest {

    // --- Fotos reales de Imagenes_prueba ---

    private val fotoPatricios = """— \
i a! | \
IE 19 \ le
EY AN |
= \ a \5 N |
ma \ \ she
Sean’,

La Malanza, Buen. 1779
Vonta: 46049421 1by i |
‘ FBAD2 AT
48193150136
FpAo2 > sco2 > O1 WS
MAR 13/10/2026 CP: 5012
micaela Machuca (CLOGISTICS91627) ln
pomicillo: Pasaje Patricios 2324, cordoba li
ly
3 5012 Ordo ba
e A"""

    private val fotoFranciscoAgusti = """(a) Francisco Agusti
Riviera Tower
Abierto 24 hs
nl Con portería.
(O Laboral.Riviera Tower Referencia: RIVIERA TOWER (al
lado de manhattan) Entre: san martin y uruguay
Q, Llamar (© WhatsApp
1 unidad
Orden en el vehículo: ATRÁS
GZ-31 Suelto
ID 48252804884
No pude entregar
Inicio Disponibles Agendados Mas
< O 101"""

    private val fotoBalbin = """16:15hs a 18:20hs
Próxima parada
Dr. Ricardo Balbin 7400 Ruta 19 Km. 329 SN
SN 9
Córdoba
Shopping ABC S.A
Abre de 08:30 a 18:00 hs.
% Ubicación validada
vr Ya llegué - Aviso automático O v
(a) GRUPO ABC
ABC
1 Con portería.
O Laboral.Parque industrial, dejar en ABC SA - Dr Ricardo
Balbin 7451 Ruta 19 Km 329 SN SN
Q, Llamar
2 unidades
Orden en el vehículo: ATRÁS
DN-30 Suelto
ID 48248761388
DN-30 Suelto
ID 48250336791
Inicio Disponibles Agendados Mas
101 O <"""

    private val fotoNuevaZelandia = """15:15hs a 18:15hs
Pendiente
Calle Nueva Zelandia 54 o
Córdoba
Edificio
% Ubicación validada
&Q Estoy llegando
(a) Naara Bono
Sin otras referencias
nal Con portería.
O Residencial.Marcar 43 en el portero electrico
Q, Llamar (© WhatsApp
1 unidad
Orden en el vehículo: ATRÁS
1D-8 Suelto
ID 48248328736
Inicio Disponibles Agendados Mas
< O 101"""

    private val fotoPatriciasArgentinas = """-
a
is “ES; y - 2
Sin y | 2
Venta: 2000018923234332 N
MN Despachar: jue 15/0ct antes de 23:59
> | 48231414822
(
E sco2 có
$ | xcF1>sco2> CBA f ms i
|
VIE 16/10/2026 |
i |
tot ga seves DBI)
| (GISELASCHEVERMAN) ES wt
Domicitio: Calle Patricias Argentinas 2275 A e
y CP: 5008 2 Er by
oot oe
Ñ h Z AN
. y
| La | |
d a ma wx i
P
A j Xe |
y ‘
PU A"""

    private val fotoTeodoro = """xd Y.
LY
¿On as
ES
SS ear eee
| i ET 20000 18988808474 >
) nn
Bas = | WA |
¡ = = PM1 48262299206
E He 26
Cy =a =
mG 00:00
== C02
“0262299206
=> renos > scoz > CBA
VIE 46/10/2026 op; 5002
Litana Lopez (118011759) A a al
Domiicilo; Teodoro ll 877 SN cordoba Ee
oP: 5002 cordoba (Gordobe) sea
cued a ere po, um
mercado
E 7"""

    @Test
    fun etiquetaPasajePatricios_extraeTodo() {
        val stop = AddressParser.parse(fotoPatricios)
        assertEquals("Pasaje Patricios 2324", stop.street)
        assertEquals("Córdoba", stop.city)
        assertEquals("5012", stop.postalCode)
        assertEquals("48193150136", stop.trackId)
        assertTrue(stop.hasAddress)
    }

    @Test
    fun capturaFranciscoAgusti_calleSinNumero() {
        val stop = AddressParser.parse(fotoFranciscoAgusti)
        assertEquals("Francisco Agusti", stop.street)
        assertEquals("", stop.city)
        assertNull(stop.postalCode)
        assertEquals("48252804884", stop.trackId)
    }

    @Test
    fun capturaBalbin_primeraDeDosUnidades() {
        val stop = AddressParser.parse(fotoBalbin)
        assertEquals("Dr. Ricardo Balbin 7400 Ruta 19 Km. 329 SN", stop.street)
        assertEquals("Córdoba", stop.city)
        assertNull(stop.postalCode)
        assertEquals("48248761388", stop.trackId)
    }

    @Test
    fun capturaNuevaZelandia() {
        val stop = AddressParser.parse(fotoNuevaZelandia)
        assertEquals("Calle Nueva Zelandia 54 o", stop.street)
        assertEquals("Córdoba", stop.city)
        assertNull(stop.postalCode)
        assertEquals("48248328736", stop.trackId)
    }

    @Test
    fun etiquetaPatriciasArgentinas_toleranteATypos() {
        val stop = AddressParser.parse(fotoPatriciasArgentinas)
        assertTrue(stop.street.startsWith("Calle Patricias Argentinas 2275"))
        assertEquals("", stop.city)
        assertEquals("5008", stop.postalCode)
        assertEquals("48231414822", stop.trackId)
    }

    @Test
    fun etiquetaTeodoro_toleranteATypos() {
        val stop = AddressParser.parse(fotoTeodoro)
        assertEquals("Teodoro ll 877 SN", stop.street)
        assertEquals("Córdoba", stop.city)
        assertEquals("5002", stop.postalCode)
    }

    // --- Casos sintéticos ---

    @Test
    fun domicilioConCiudadLarga_noCortaEnCalleQueEsCiudad() {
        val stop = AddressParser.parse(
            "Domicilio: Av. Corrientes 1234, Ciudad Autónoma de Buenos Aires\nC.P. C1425ABC"
        )
        assertEquals("Av. Corrientes 1234", stop.street)
        assertEquals("Ciudad Autónoma de Buenos Aires", stop.city)
        assertEquals("C1425ABC", stop.postalCode)
    }

    @Test
    fun varianteOpConCpSolo_noTomaCpComoCalle() {
        val stop = AddressParser.parse("oP: 5002\ncordoba")
        assertEquals("", stop.street)
        assertEquals("Córdoba", stop.city)
        assertEquals("5002", stop.postalCode)
    }

    @Test
    fun typoDomicitioSinCiudad() {
        val stop = AddressParser.parse("Domicitio: Av. Siempre Viva 742")
        assertEquals("Av. Siempre Viva 742", stop.street)
        assertEquals("", stop.city)
        assertNull(stop.postalCode)
    }

    @Test
    fun textoVacio_devuelveStopVacio() {
        val stop = AddressParser.parse("")
        assertEquals("", stop.street)
        assertEquals("", stop.city)
        assertNull(stop.postalCode)
        assertNull(stop.trackId)
        assertFalse(stop.hasAddress)
    }

    @Test
    fun soloRuidoDeUI_devuelveStopVacio() {
        val stop = AddressParser.parse(
            "Inicio Disponibles Agendados\n1 unidad\nOrden en el vehículo: ATRÁS"
        )
        assertFalse(stop.hasAddress)
    }
}
