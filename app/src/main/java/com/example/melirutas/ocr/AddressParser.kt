package com.example.melirutas.ocr

/**
 * Extrae dirección, localidad, CP y track ID del texto crudo de OCR de una foto
 * (captura de la app de reparto o etiqueta de envío de Mercado Libre).
 *
 * Es tolerante a los errores típicos de Tesseract sobre fotos de pantalla:
 * "Domicitio:", "Pomicillo:", "oP: 5002", líneas de ruido de la UI, etc.
 * Si no puede identificar la calle, devuelve street en blanco para que se
 * corrija manualmente en la pantalla de revisión.
 *
 * Kotlin puro (sin Android): testeable en tests JVM.
 */
object AddressParser {

    /** Dirección parseada de una foto. Los campos vacíos requieren edición manual. */
    data class ParsedStop(
        val street: String,
        val city: String,
        val postalCode: String?,
        val trackId: String?
    ) {
        val hasAddress: Boolean get() = street.isNotBlank()
    }

    private val CP_REGEX = Regex("(?i)\\b(?:c|o)\\.?\\s*p\\.?\\s*[:;]?\\s*([a-z]?\\d{4}[a-z]{0,3})\\b")
    private val CP_ONLY_LINE_REGEX =
        Regex("(?i)^(?:c|o)\\.?\\s*p\\.?\\s*[:;]?\\s*[a-z]?\\d{4}[a-z]{0,3}\\s*$")
    private val DOMICILIO_REGEX = Regex("(?i)[dhp][a-z]{2,10}i[ci][a-z]{0,6}o\\s*[:;.,]\\s*(.+)$")
    private val TRACK_ID_REGEX = Regex("(?i)\\bID\\s*(\\d{9,15})\\b")
    private val STANDALONE_ID_REGEX = Regex("^\\d{11,15}$")
    private val DATE_REGEX = Regex("\\d{1,2}/\\d{1,2}/\\d{4}")
    private val ICON_PREFIX_REGEX = Regex("^\\([a-zA-Z0-9]\\)\\s*")
    private val WHITESPACE_REGEX = Regex("\\s+")

    private val NOISE_REGEXES = listOf(
        Regex("(?i)^\\d{1,2}[:.]\\d{2}\\s*hs"),
        Regex("(?i)^(pendiente|pr[oó]xima\\s+parada|no\\s+pude\\s+entregar|entregado|finalizado)\\b"),
        Regex("(?i)orden\\s+en\\s+el\\s+veh"),
        Regex("(?i)^(inicio|disponibles|agendados|ma[sá]s)\\b"),
        Regex("(?i)\\bunidad(?:es)?\\b"),
        Regex("(?i)\\bsuelto\\b"),
        Regex("(?i)ubicaci[oó]n\\s+validada"),
        Regex("(?i)(estoy\\s+llegando|ya\\s+llegu[eé]|aviso\\s+autom[aá]tico)"),
        Regex("(?i)(llamar|whatsapp)"),
        Regex("(?i)(porter[ií]a|residencial|laboral|edificio|referencia)"),
        Regex("(?i)(abre\\s+de|abierto|horario)"),
        Regex("(?i)despachar"),
        Regex("\\d{1,2}/\\d{1,2}/\\d{4}"),
        Regex("(?i)^gp\\s*:"),
        Regex("(?i)^venta\\s*:"),
        Regex(">"),
        Regex("(?i)^id\\s*\\d"),
        Regex("[\\\\|]"),
        CP_ONLY_LINE_REGEX
    )

    private val TRIM_EDGE_CHARS =
        setOf('|', '\\', '/', '"', '\'', '“', '”', '‘', '’', '¿', '¡', '<', '>', '%', '$', '@', '*', '~', '+', '=', '_', '[', ']', '{', '}', ';', '.', ',', ':', '!', '?')

    private val ACCENT_MAP = mapOf(
        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n'
    )

    private val CITIES = listOf(
        "Córdoba", "Río Cuarto", "Villa Allende", "Alta Gracia", "Villa María", "Cosquín",
        "Villa Carlos Paz", "San Francisco", "Justiniano Posse", "La Calera", "Jesús María",
        "Mina Clavero", "Villa Dolores", "Embalse", "General Pico", "San Rafael", "Malargüe",
        "Ciudad Autónoma de Buenos Aires", "Capital Federal", "Buenos Aires", "La Plata",
        "Rosario", "Santa Fe", "Mendoza", "Salta", "Tucumán", "Neuquén", "Bariloche",
        "Puerto Madryn", "Corrientes", "Posadas", "Resistencia", "Bahía Blanca"
    )

    fun parse(ocrText: String): ParsedStop {
        val lines = ocrText.lineSequence().mapNotNull { normalizeLine(it) }.toList()

        val postalCode = lines.firstNotNullOfOrNull { line ->
            CP_REGEX.find(line)?.groupValues?.get(1)?.uppercase()
        }
        val trackId = lines.firstNotNullOfOrNull { line ->
            TRACK_ID_REGEX.find(line)?.groupValues?.get(1)
                ?: if (STANDALONE_ID_REGEX.matches(line)) line else null
        }

        var street = ""
        var city = ""

        val domicilioLine = lines.firstOrNull { DOMICILIO_REGEX.containsMatchIn(it) }
        if (domicilioLine != null) {
            val raw = DOMICILIO_REGEX.find(domicilioLine)!!.groupValues[1].trim()
            val (s, c) = splitCity(raw)
            street = s
            city = c
        }

        val content = lines.filter { isContentLine(it) && !isNoiseLine(it) }

        if (street.isBlank()) {
            val cityIndex = content.indexOfFirst { isCityLine(it) }
            val streetIndex = content.indexOfFirst { looksLikeStreet(it) }
            street = when {
                streetIndex >= 0 && (cityIndex < 0 || streetIndex < cityIndex) -> content[streetIndex]
                cityIndex > 0 -> content[cityIndex - 1]
                else -> content.firstOrNull { !isCityLine(it) } ?: ""
            }
            if (cityIndex >= 0 && street.isNotBlank()) {
                city = canonicalCity(content[cityIndex])
            }
        }

        if (city.isBlank()) {
            city = content.firstOrNull { isCityLine(it) }?.let { canonicalCity(it) } ?: ""
        }

        return ParsedStop(
            street = street.trim(),
            city = city,
            postalCode = postalCode,
            trackId = trackId
        )
    }

    private fun normalizeLine(raw: String): String? {
        var s = WHITESPACE_REGEX.replace(raw.replace('\t', ' '), " ").trim()
        s = ICON_PREFIX_REGEX.replace(s, "")
        s = s.trim { it.isWhitespace() || it in TRIM_EDGE_CHARS }
        s = WHITESPACE_REGEX.replace(s, " ").trim()
        return s.ifBlank { null }
    }

    private fun isNoiseLine(line: String): Boolean = NOISE_REGEXES.any { it.containsMatchIn(line) }

    private fun isContentLine(line: String): Boolean = line.any { it.isLetter() } && line.length >= 3

    private fun looksLikeStreet(line: String): Boolean {
        val letters = line.count { it.isLetter() }
        if (line.length < 6 || letters < 3 || !line.any { it.isDigit() }) {
            return false
        }
        if (CP_REGEX.containsMatchIn(line) || DATE_REGEX.containsMatchIn(line)) {
            return false
        }
        return true
    }

    private fun isCityLine(line: String): Boolean =
        CITIES.any { normalizePhrase(it) == normalizePhrase(line) }

    private fun canonicalCity(line: String): String =
        CITIES.firstOrNull { normalizePhrase(it) == normalizePhrase(line) } ?: line

    /**
     * Busca una ciudad del diccionario dentro del texto (p. ej. "Teodoro ll 877 SN cordoba")
     * y devuelve la dirección hasta ella y la ciudad canónica. Prefiere el corte que deja
     * una calle con número (p. ej. "Av. Corrientes 1234, Ciudad Autónoma de Buenos Aires"
     * no debe cortar en "Corrientes").
     */
    private fun splitCity(text: String): Pair<String, String> {
        val words = text.split(' ')
        val normalizedWords = words.map { normalizeWord(it) }

        data class Candidate(val hasDigitBefore: Boolean, val index: Int, val cityWords: Int, val city: String, val before: String)

        val candidates = ArrayList<Candidate>()
        for (i in words.indices) {
            if (i == 0) continue
            for (city in CITIES) {
                val cityWordList = city.split(' ').map { normalizeWord(it) }
                val end = i + cityWordList.size
                if (end <= normalizedWords.size && normalizedWords.subList(i, end) == cityWordList) {
                    val before = words.subList(0, i)
                        .joinToString(" ")
                        .trim { it.isWhitespace() || it in TRIM_EDGE_CHARS }
                    if (before.isNotBlank()) {
                        candidates += Candidate(
                            hasDigitBefore = before.any { it.isDigit() },
                            index = i,
                            cityWords = cityWordList.size,
                            city = city,
                            before = before
                        )
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return text.trim { it.isWhitespace() || it in TRIM_EDGE_CHARS } to ""
        }
        val best = candidates.sortedWith(
            compareByDescending<Candidate> { it.hasDigitBefore }
                .thenBy { it.index }
                .thenByDescending { it.cityWords }
        ).first()
        return best.before to best.city
    }

    private fun normalizeWord(word: String): String =
        deaccent(word.lowercase()).filter { it in 'a'..'z' || it in '0'..'9' }

    private fun normalizePhrase(text: String): String =
        deaccent(text.lowercase())
            .replace(WHITESPACE_REGEX, " ")
            .trim()
            .trim { it == '.' || it == ',' || it == ';' || it == ':' }

    private fun deaccent(text: String): String = buildString(text.length) {
        for (c in text) {
            append(ACCENT_MAP[c] ?: c)
        }
    }
}
