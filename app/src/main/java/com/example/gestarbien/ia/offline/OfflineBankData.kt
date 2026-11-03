package com.example.gestarbien.ia.offline

import kotlin.random.Random

// ============================================================
// GESTARBIEN - OFFLINE DATA (PROFESSIONAL VERSION) - MEJORADA
// ============================================================
// Base de datos local de respuestas con enfoque narrativo
// y terapia conversacional para madres gestantes y puérperas.
// ============================================================

// ============================================================
// TAG KEYWORDS
// ============================================================
internal val TAG_KEYWORDS: Map<String, List<String>> = mapOf(
    "mala_madre" to listOf(
        "mala madre", "mala mama", "no soy suficiente", "no sere suficiente",
        "fallarle a mi bebe", "no soy capaz", "fracasada", "fracasado"
    ),
    "culpa" to listOf(
        "me siento culpable", "siento culpa", "me da culpa", "es mi culpa", "culpable"
    ),
    "soledad" to listOf(
        "sola", "solo", "nadie me apoya", "nadie me entiende", "no tengo a nadie"
    ),
    "llanto" to listOf(
        "llorar", "lloro", "no paro de llorar", "quiero llorar", "llorando"
    ),
    "miedo_parto" to listOf(
        "parto", "dolor del parto", "miedo al parto", "salud del bebe",
        "complicaciones", "nacer", "cesarea", "temor al parto"
    ),
    "futuro" to listOf(
        "que va a pasar", "el futuro", "no se como hare", "sobrepensando",
        "incertidumbre", "no podre con todo"
    ),
    "sintomas_fisicos" to listOf(
        "presion en el pecho", "falta de aire", "taquicardia", "corazon acelerado",
        "no puedo respirar", "temblor", "tension", "ahogo"
    ),
    "salud_bebe" to listOf(
        "enfermo", "enferma", "fiebre", "tos", "no come", "vomito",
        "respiracion", "le duele", "pediatra", "vacuna", "sintomas"
    ),
    "hipervigilancia" to listOf(
        "ver si respira", "mirarlo respirar", "no puedo dormir por velarlo",
        "revisarlo", "chequearlo", "no le quito la mirada", "velar"
    ),
    "miedo_perderlo" to listOf(
        "se me muera", "le pase algo malo", "le pase algo", "muerte lactante",
        "caiga", "hacerle daño", "pensamiento feo", "escenario horrible"
    ),
    "falta_sueno" to listOf(
        "no he dormido", "sin dormir", "desvelo", "desvelada", "insomnio",
        "despertar", "no duerme", "noches terribles", "pocas horas de sueño", "sueño acumulado"
    ),
    "sobrecarga" to listOf(
        "muchas cosas", "carga mental", "hacer todo sola", "nadie me ayuda",
        "trabajo y bebe", "multitarea", "superada por las tareas", "sola con todo"
    ),
    "burnout_maternal" to listOf(
        "no puedo mas", "no doy mas", "al limite", "harta", "colapso",
        "explotar", "agotamiento extremo", "desbordada de cansancio", "quebrada"
    ),
    "llanto_bebe" to listOf(
        "no para de llorar", "llora y llora", "no se calma", "llanto descontrolado",
        "no se que le pasa", "gritando", "no se duerme"
    ),
    "expectativa_vs_realidad" to listOf(
        "nada sale bien", "arruinado el dia", "no pude hacer nada", "planes arruinados",
        "no me rinde", "desastre"
    ),
    "sin_control" to listOf(
        "impotencia", "no puedo hacer nada", "fuera de control", "no tengo el control",
        "nada funciona"
    ),
    "exceso_consejos" to listOf(
        "me dicen una cosa y otra", "opiniones", "mi suegra", "mi mama", "en internet leique",
        "todo el mundo opina", "demasiados consejos"
    ),
    "niebla_mental" to listOf(
        "olvido todo", "se me olvidan las cosas", "niebla mental", "desmemoriada",
        "distraida", "no me puedo concentrar", "cabeza en blanco"
    ),
    "incertidumbre_decisiones" to listOf(
        "no se que decidir", "no se que hacer", "tengo duda", "temor a equivocarme",
        "cual es la mejor opcion", "indecisa"
    ),
    "arrepentimiento_temporal" to listOf(
        "me arrepiento", "no debi", "arrepentida", "desearia no haber quedado embarazada",
        "no quiero ser mama", "quisiera volver atras", "no debi tenerlo", "error haber quedado"
    ),
    "extrana_vida_previa" to listOf(
        "extraño mi vida de antes", "mi vida anterior", "mi libertad", "extraño mi trabajo",
        "quien era antes", "no tengo tiempo para mi", "perdi mi vida", "mi tiempo"
    ),
    "miedo_al_cambio" to listOf(
        "no encajo", "no nací para esto", "la maternidad no es para mi",
        "todo cambio", "mi vida cambio demasiado", "no me hallo"
    ),
    "signos_alarma" to listOf(
        "sangrado", "sangre", "fiebre", "fuerte dolor", "dolor agudo",
        "perdida de liquido", "no se mueve el bebe", "sin movimientos", "contracciones fuertes"
    ),
    "preeclampsia_alert" to listOf(
        "dolor de cabeza muy fuerte", "veo luces", "lucitas", "zumbido en oidos",
        "pitido", "cara hinchada", "manos muy hinchadas", "tension alta", "presion alta"
    ),
    "malestar_digestivo" to listOf(
        "vomito", "vomitando", "nauseas", "reflujo", "acidez", "no puedo comer", "no retengo agua"
    ),
    "dolor_lactancia" to listOf(
        "me duele amamantar", "grietas", "pecho rojo", "pecho caliente", "mastitis",
        "dolor en el pezon", "me sale sangre del pezon", "congestion mamaria"
    ),
    "culpa_formula_destete" to listOf(
        "no tengo leche", "baja produccion", "dar formula", "dar tetero", "no quiero amamantar",
        "quiero destetar", "culpa por no darle pecho", "se queda con hambre"
    ),
    "rechazo_imagen" to listOf(
        "me veo fea", "subi mucho de peso", "gorda", "estrias", "flacidez",
        "no me gusta mi cuerpo", "asco de cuerpo", "extraño mi cuerpo de antes", "cuerpo arruinado"
    ),
    "fatiga_extrema_fisica" to listOf(
        "cuerpo molido", "agotada fisicamente", "no puedo moverme", "dolor de espalda", "sin energia"
    ),
    "perdida_autonomia" to listOf(
        "no tengo tiempo", "perdi mi libertad", "ya no soy yo", "trabajo pausado",
        "dependencia", "atada al bebe", "extraño mi rutina", "sin tiempo libre"
    ),
    "invisibilizacion" to listOf(
        "nadie me pregunta como estoy", "solo importa el bebe", "me siento invisible",
        "a nadie le importo", "solo me ven como mama"
    ),
    "desconexion_afectiva" to listOf(
        "no siento nada", "no lo quiero", "no la quiero", "desconectada del bebe",
        "no siento ese amor", "bebe extraño", "no siento conexion", "frio con mi bebe"
    ),
    "dificultad_llanto" to listOf(
        "no soporto el llanto", "el llanto me desespera", "quiero salir corriendo cuando llora",
        "me altera que llore", "el llanto me sobrepasa"
    ),
    "sindrome_impostora" to listOf(
        "soy mala mama", "no se cuidar a mi bebe", "no naci para esto",
        "le hago daño", "lo hago todo mal", "no soy capaz", "incompetente"
    ),
    "juicio_externo" to listOf(
        "me critican", "mi suegra me dice", "mi mama me critica", "opiniones de los demas",
        "me dicen que lo hago mal", "comentarios de la gente", "presion familiar"
    ),
    "soledad_absoluta" to listOf(
        "no tengo a nadie", "estoy sola en el mundo", "sin familia", "sin pareja",
        "nadie me acompaña", "aislada", "completamente sola"
    ),
    "acompanada_pero_sola" to listOf(
        "mi pareja no ayuda", "mi esposo no entiende", "rodeada de gente pero sola",
        "vivo con gente pero igual", "nadie me apoya en casa", "no me entienden en mi casa"
    ),
    "dificultad_pedir_ayuda" to listOf(
        "me da pena pedir ayuda", "no me gusta molestar", "debo poder sola",
        "orgullo", "no quiero ser una carga", "me cuesta pedir"
    )
)

// ============================================================
// KEYWORD GROUPS
// ============================================================
internal val KEYWORD_GROUPS: Map<String, List<String>> = mapOf(
    "saludo" to listOf(
        "hola", "holi", "buenas", "buenos dias", "buen dia",
        "buenas tardes", "buenas noches", "hola quiero hablar",
        "quiero hablar", "quiero hablar un rato", "necesito hablar",
        "puedo hablar", "puedo hablar un momento", "buen día"
    ),
    "cierre" to listOf(
        "gracias", "gracias por acompañarme", "quiero cerrar",
        "cerrar", "terminemos", "hasta luego", "eso era todo",
        "podemos cerrar", "sigamos luego", "no quiero hablar mas",
        "por ahora esta bien", "asi esta bien", "esta bien asi",
        "nop asi esta bien", "no gracias", "no por ahora",
        "me voy", "chao", "adios", "adiós"
    ),
    "pedir_mas_informacion" to listOf(
        "dime mas", "quiero que me digas mas", "cuentame mas",
        "hablame mas", "explicame", "que hago", "que puedo hacer",
        "ayudame", "necesito ayuda", "dime algo", "quiero entender",
        "quiero saber mas", "quiero que me expliques", "no entiendo"
    ),
    "pregunta_por_que" to listOf("porque", "por que", "por qué", "xq", "pq"),
    "respuesta_corta" to listOf(
        "si", "no", "no se", "tal vez", "puede ser", "creo",
        "no entiendo", "mmm", "no sabria", "no sabria decir",
        "no se que contestarte", "no se que decir", "no tengo palabras",
        "ok", "vale", "bueno"
    ),
    "riesgo_alto" to listOf(
        "no quiero vivir", "quiero morir", "me quiero morir",
        "quitarme la vida", "hacerme dano", "hacerme daño",
        "desaparecer para siempre", "no quiero seguir viviendo",
        "seria mejor no estar", "ya no quiero existir",
        "quiero desaparecer", "me voy a hacer dano", "me voy a hacer daño",
        "no le veo sentido", "no tengo fuerzas para seguir",
        "matarme", "me quiero matar", "quiero matarme", "me voy a matar",
        "me voy a matarme", "quiero matar", "suicidarme", "quiero suicidarme",
        "pensamientos suicidas", "ideas suicidas", "suicidio",
        "acabar con mi vida", "terminar con mi vida", "quitarme la vida ahora",
        "acabar con todo", "no quiero estar aqui", "no quiero estar aquí",
        "no aguanto mas vivir", "prefiero estar muerta", "prefiero estar muerto",
        "mejor muerta", "mejor muerto, "
    ),
    "riesgo_medio" to listOf(
        "no puedo con esto", "esto me supera", "todo me supera",
        "no tengo fuerzas", "me siento al limite", "estoy al limite",
        "no se si puedo seguir", "ya no puedo mas", "no aguanto mas",
        "no puedo mas", "me siento sin salida", "esto es demasiado"
    ),
    // ============================================================
    // FIX: bienestar — mensajes positivos que antes caían en
    // "desconocido" y el bot respondía como si estuviera mal.
    // ============================================================
    "bienestar" to listOf(
        "me siento bien", "estoy bien", "me siento mejor", "hoy estoy bien",
        "estoy tranquila", "me siento tranquila", "estoy contenta",
        "me siento contenta", "estoy feliz", "me siento feliz",
        "hoy fue un buen dia", "estoy mejor", "me siento en calma",
        "estoy en calma", "todo bien", "me siento positiva",
        "me siento animada", "estoy animada", "me siento con energia",
        "estoy con energia", "hoy estoy tranquila", "hoy me siento bien"
    ),
    "sintoma_fisico" to listOf(
        "vomito", "vomitar", "vomitando", "nauseas", "mareo",
        "mareada", "mareado", "dolor", "me duele", "sangrado",
        "sangrar", "fiebre", "contracciones", "presion alta",
        "no puedo retener liquidos", "me siento debil", "debilidad",
        "dolor fuerte", "dolor abdominal", "dolor en el vientre",
        "dolor en la barriga", "me baja sangre", "sangre",
        "cuerpo me duele", "mi cuerpo duele",
        "me duele el cuerpo", "me duele la cabeza",
        "me duele la espalda", "me duele el estomago"
    ),
    "lactancia" to listOf(
        "leche", "pecho", "dar pecho", "amamantar", "lactancia",
        "no tengo leche", "poca leche", "pecho duele", "pezon",
        "bebe no toma", "bebé no toma", "produzco leche",
        "no produzco", "amamantando", "dar el pecho"
    ),
    "cuerpo_cambios" to listOf(
        "cambiando", "cambió", "transformando", "cambios",
        "reconocerme", "reconozco", "espejo",
        "no reconozco mi cuerpo", "cuerpo cambia",
        "cuerpo diferente", "mi cuerpo no es el mismo",
        "no me gusta mi cuerpo", "no me gusta verme",
        "no me siento yo", "no soy la misma",
        "fea", "feo", "gorda", "gordo",
        "estrias", "flacidez", "me veo mal",
        "mi cuerpo esta", "mi cuerpo se siente",
        "no me reconozco", "me veo diferente"
    ),
    "negacion_duda" to listOf(
        "no quiero", "no se si quiero", "no se si quiera",
        "no estoy segura", "no estoy seguro", "me arrepiento",
        "no queria", "no estaba lista", "no estaba listo",
        "no estaba preparada", "no estaba preparado",
        "no me siento lista", "no me siento listo",
        "me cuesta aceptar", "dudo", "y si no tengo",
        "si no tengo", "que pasa si no tengo",
        "que pasa si no lo tengo", "que pasa si no la tengo",
        "que pasa si no continuo", "que pasa si no sigo",
        "no tener", "no tenerlo", "no tenerla",
        "no seguir", "no continuar", "no se si tenerlo",
        "no se si tenerla", "no se si seguir", "no se si continuar"
    ),
    "embarazo_bebe" to listOf(
        "bebe", "bebito", "bebita", "mi bebe", "al bebe",
        "el bebe", "la bebe", "embarazo", "embarazada",
        "gestacion", "hijo", "hija", "mi hijo", "mi hija",
        "tenerlo", "tenerla", "mama", "madre",
        "parto", "dar a luz", "cesarea", "contracciones",
        "dilatación", "hospital", "clinica", "maternidad"
    ),
    "malestar" to listOf(
        "mal", "me siento mal", "estoy mal", "no me siento bien",
        "fatal", "horrible", "todo me pesa", "todo me afecta",
        "apagada", "apagado", "bajoneada", "bajoneado",
        "me siento rara", "me siento raro"
    ),
    "ansiedad" to listOf(
        "ansiedad", "ansiosa", "ansioso", "miedo", "preocupa",
        "preocupada", "preocupado", "nerviosa", "nervioso",
        "asustada", "asustado", "panico", "no dejo de pensar",
        "mi cabeza no para", "tengo la cabeza llena",
        "intranquila", "intranquilo",
        "parto", "dar a luz", "cesarea", "contracciones",
        "algo le pase", "algo pase", "hospital", "clinica",
        "miedo al parto", "miedo a no poder"
    ),
    "cansancio" to listOf(
        "cansada", "cansado", "muy cansada", "muy cansado",
        "agotada", "agotado", "agotamiento", "sin energia",
        "sin energía", "no tengo energia", "no tengo energía",
        "no dormi", "no dormí", "dormi mal", "dormí mal",
        "no pude dormir", "me siento agotada", "me siento agotado",
        "estoy agotada", "estoy agotado", "estoy muy cansada",
        "estoy muy cansado", "me pesa el cuerpo", "no me dan fuerzas",
        "no tengo fuerzas", "me siento sin fuerzas",
        "me siento muy cansada", "me siento muy cansado",
        "no descanso", "no puedo descansar"
    ),
    "tristeza" to listOf(
        "triste", "tristeza", "sola", "solo", "vacia", "vacio",
        "llorar", "lloro", "sin animo", "desanimada", "desanimado",
        "derrotada", "derrotado", "abandonada", "abandonado",
        "bebe llora", "bebé llora", "no puedo con mi bebe",
        "no puedo con el bebe", "no puedo con mi bebé",
        "no tengo ganas", "no quiero hacer nada"
    ),
    "ansiedad_bebe" to listOf(
        "bebe", "bebé", "mi bebe", "mi bebé",
        "algo le pase", "algo pase",
        "preocupo por mi bebe", "preocupo por mi bebé",
        "miedo de que le pase", "miedo que le pase",
        "miedo por mi bebe", "miedo por mi bebé"
    ),
    "frustracion" to listOf(
        "frustrada", "frustrado", "harta", "harto",
        "sobrepasada", "sobrepasado", "colapsada", "colapsado",
        "saturada", "saturado", "todo me molesta", "ya no se que hacer",
        "no puedo mas", "no aguanto", "me desespero"
    ),
    "confusion" to listOf(
        "confundida", "confundido", "confusion",
        "no se como sentirme", "no se que sentir",
        "no entiendo lo que siento", "emociones mezcladas",
        "no se que pensar", "no se que hacer", "perdida",
        "perdido", "no logro entenderme", "no se que me pasa"
    ),
    "identidad_materna" to listOf(
        "ser mama", "buena mama", "mala mama", "buena madre",
        "mala madre", "me siento culpable", "siento culpa",
        "me da culpa", "fallarle a mi bebe", "no sere suficiente",
        "quiero hacerlo bien", "fracasada", "fracasado",
        "no soy suficiente", "no valgo nada",
        "no soy capaz", "no puedo con todo", "soy mala madre"
    ),
    "identidad_postparto" to listOf(
        "posparto", "postparto", "despues del parto",
        "no soy la misma", "ya no soy la misma",
        "perdí mi identidad", "perdi mi identidad",
        "no se quien soy", "no sé quién soy",
        "no me reconozco", "ya no soy yo"
    ),
    "vinculo_bebe" to listOf(
        "quiero hablarle a mi bebe", "quiero decirle algo a mi bebe",
        "pienso en mi bebe", "cuando nazca", "quiero conectar",
        "me cuesta conectar", "quiero imaginar a mi bebe",
        "quiero escribirle a mi bebe", "no siento conexion",
        "no siento nada", "no conecto con mi bebe"
    ),
    "red_apoyo" to listOf(
        "mi pareja", "mi mama", "mi familia", "mi amiga",
        "me apoya", "me acompaña", "me escucha", "nadie me apoya",
        "nadie me entiende", "nadie me escucha", "no tengo apoyo",
        "necesito hablar con alguien",
        "no tengo a nadie", "estoy sola", "estoy solo",
        "no tengo a quien", "vive lejos", "familia lejos",
        "no tengo con quien hablar", "estoy desamparada"
    ),
    "desahogo" to listOf(
        "desahogarme", "desahogar",
        "solo quiero hablar", "necesito hablar",
        "quiero contar lo que siento",
        "necesito sacar esto",
        "no quiero consejos", "no quiero soluciones",
        "solo escuchame", "solo escúchame",
        "quiero llorar", "necesito llorar",
        "no tengo con quien hablar", "solo necesito que me escuchen"
    ),
    "ambivalencia_embarazo" to listOf(
        "no se si quiero", "no se si quiera",
        "no estoy segura", "no estoy seguro",
        "me arrepiento", "no queria",
        "no estaba lista", "no estaba listo",
        "no estaba preparada", "no estaba preparado",
        "no me siento lista", "no me siento listo",
        "me cuesta aceptar", "dudo", "no se si quiero ser mama"
    )
)

// ============================================================
// CATEGORY PRIORITY
// ============================================================
internal val CATEGORY_PRIORITY: Map<String, Int> = mapOf(
    "riesgo_alto" to 100,
    "ambivalencia_embarazo" to 95,
    "riesgo_medio" to 90,
    "sintoma_fisico" to 82,
    "lactancia" to 80,
    "cuerpo_cambios" to 78,
    "ansiedad_bebe" to 74,
    "ansiedad" to 72,
    "tristeza" to 68,
    "cansancio" to 66,
    "frustracion" to 64,
    "confusion" to 62,
    "malestar" to 60,
    "desahogo_sin_apoyo" to 58,
    "identidad_postparto" to 56,
    "desahogo" to 54,
    "identidad_materna" to 52,
    "red_apoyo" to 50,
    "vinculo_bebe" to 46,
    // FIX: bienestar con prioridad media-baja, para que no pise
    // categorías con más urgencia (tristeza, ansiedad, etc.) pero
    // gane a "desconocido".
    "bienestar" to 45,
    "cierre" to 40,
    "pedir_mas_informacion" to 35,
    "pregunta_por_que" to 34,
    "respuesta_corta" to 30,
    "saludo" to 20,
    "desconocido" to 0,
)

// ============================================================
// CATEGORY ALIASES
// ============================================================
internal val CATEGORY_ALIASES: Map<String, String> = mapOf(
    "general" to "desconocido",
    "desconocido" to "desconocido",
    "estado_emocional" to "malestar",
    "respuesta_corta" to "respuesta_corta",
    "pedir_mas_informacion" to "pedir_mas_informacion",
    "pregunta_por_que" to "pregunta_por_que",
    "sintoma_fisico_followup" to "sintoma_fisico",
    "ambivalencia_followup" to "ambivalencia_followup",
    "multitema_ambivalencia_ansiedad" to "ambivalencia_embarazo",
    "multitema_ambivalencia_sintoma_ansiedad" to "multitema_ambivalencia_sintoma_ansiedad",
    "multitema_sintoma_ansiedad" to "multitema_sintoma_ansiedad",
    "multitema_tristeza_ansiedad" to "multitema_tristeza_ansiedad",
)

// ============================================================
// RISK / EMOTION / INTENSITY
// ============================================================
internal val RISK_CATEGORIES: Map<String, String> = mapOf(
    "riesgo_alto" to "alto",
    "riesgo_medio" to "medio",
    "sintoma_fisico" to "medio",
    "lactancia" to "medio",
    "cuerpo_cambios" to "medio",
    "ambivalencia_embarazo" to "medio",
    "ambivalencia_followup" to "medio",
    "multitema_ambivalencia_sintoma_ansiedad" to "medio",
    "multitema_sintoma_ansiedad" to "medio",
)

internal val EMOTION_BY_CATEGORY: Map<String, String> = mapOf(
    "saludo" to "neutral",
    "cierre" to "neutral",
    "malestar" to "malestar",
    "tristeza" to "tristeza",
    "ansiedad" to "ansiedad",
    "ansiedad_bebe" to "ansiedad",
    "cansancio" to "cansancio",
    "frustracion" to "frustracion",
    "confusion" to "confusion",
    "red_apoyo" to "soledad",
    "vinculo_bebe" to "vinculo",
    "identidad_materna" to "culpa",
    "identidad_postparto" to "culpa",
    "ambivalencia_embarazo" to "confusion",
    "ambivalencia_followup" to "confusion",
    "sintoma_fisico" to "malestar",
    "lactancia" to "malestar",
    "cuerpo_cambios" to "malestar",
    "riesgo_medio" to "malestar",
    "riesgo_alto" to "desesperanza",
    "bienestar" to "neutral",
    "desconocido" to "neutral",
    "respuesta_corta" to "neutral",
    "pedir_mas_informacion" to "neutral",
    "pregunta_por_que" to "neutral",
    "multitema_tristeza_ansiedad" to "ansiedad",
    "multitema_sintoma_ansiedad" to "ansiedad",
    "multitema_ambivalencia_sintoma_ansiedad" to "confusion",
    "desahogo" to "neutral",
    "desahogo_sin_apoyo" to "neutral",
)

internal val INTENSITY_BY_CATEGORY: Map<String, String> = mapOf(
    "saludo" to "baja",
    "cierre" to "baja",
    "desconocido" to "baja",
    "respuesta_corta" to "baja",
    "pedir_mas_informacion" to "media",
    "pregunta_por_que" to "media",
    "malestar" to "media",
    "tristeza" to "media",
    "ansiedad" to "media",
    "ansiedad_bebe" to "media",
    "cansancio" to "media",
    "frustracion" to "media",
    "confusion" to "media",
    "red_apoyo" to "media",
    "vinculo_bebe" to "media",
    "identidad_materna" to "media",
    "identidad_postparto" to "media",
    "ambivalencia_embarazo" to "alta",
    "ambivalencia_followup" to "alta",
    "sintoma_fisico" to "alta",
    "lactancia" to "alta",
    "cuerpo_cambios" to "alta",
    "riesgo_medio" to "alta",
    "riesgo_alto" to "alta",
    "bienestar" to "baja",
    "multitema_tristeza_ansiedad" to "media",
    "multitema_sintoma_ansiedad" to "alta",
    "multitema_ambivalencia_sintoma_ansiedad" to "alta",
    "desahogo" to "media",
    "desahogo_sin_apoyo" to "media",
)

// ============================================================
// DIRECT RESPONSES
// ============================================================
internal val DIRECT_RESPONSES: Map<String, List<String>> = mapOf(
    "saludo" to listOf(
        "Hola{name}. Soy GestarBien, tu acompañante en este espacio. Estoy aquí para escucharte con calma y sin juicios. Cuéntame, ¿qué está pasando hoy?",
        "Hola{name}. Gracias por estar aquí. Este es un espacio seguro para ti. ¿Qué te gustaría compartir hoy?",
        "Hola{name}. No necesitas tener las palabras perfectas para empezar. Puedes contarme lo que sea que esté pesando en este momento.",
        "Bienvenida{name}. Estoy aquí para acompañarte. ¿Cómo te sientes hoy? Puedes empezar por donde quieras."
    ),
    "cierre" to listOf(
        "Gracias por compartir este momento conmigo. Puedes volver cuando lo necesites; aquí estaré para acompañarte con calma.",
        "Ha sido un honor acompañarte hoy. Recuerda que este espacio es tuyo. Hasta pronto.",
        "Cerramos por ahora. Lo que has compartido es importante y merece ser escuchado. Cuídate.",
        "Gracias por confiar en este espacio. Cuando quieras retomar, estaré aquí. Hasta luego."
    )
)

// ============================================================
// SAFETY RESPONSES
// ============================================================
internal val SAFETY_RESPONSES: Map<String, List<String>> = mapOf(
    "riesgo_alto" to listOf(
        "Siento mucho que estés atravesando este momento tan difícil. Lo que estás sintiendo merece ser tomado con toda la seriedad que tiene. Por favor, no lo enfrentes sola: busca a una persona de confianza ahora mismo o contacta a los servicios de emergencia de tu país. Tu seguridad es lo más importante. ¿Hay alguien cerca de ti en este momento?",
        "Gracias por tener la valentía de compartir esto conmigo. No estás sola en esto, pero necesito que sepas que este espacio es para acompañarte, no para reemplazar la ayuda que necesitas. Si estás en Colombia, puedes llamar a la [[tel:106:Línea 106]] (Salud Mental) o al [[tel:123:123]] (Emergencias). ¿Puedes hacerlo?",
        "Lo que estás diciendo merece una atención inmediata. No tienes que resolverlo todo sola. Por favor, contacta a una persona de confianza, llama a una línea de emergencia o acude al centro de salud más cercano. En Colombia puedes marcar [[tel:106:Línea 106]] o [[tel:123:123]]. Tu vida y tu bienestar son lo primero. ¿Puedes pedir ayuda ahora?"
    ),
    "riesgo_medio" to listOf(
        "Lo que estás sintiendo es muy pesado para cargarlo sola. Este espacio está aquí para ti, pero también es importante que puedas sumar apoyo humano. Piensa en alguien de confianza con quien puedas hablar hoy. ¿Hay alguien a quien puedas escribirle ahora?",
        "No quiero que te quedes sola con esta carga. Podemos seguir hablando, pero también necesitas apoyo fuera de aquí. ¿Hay alguna persona en quien confíes y que pueda estar cerca de ti en este momento?",
        "Te escucho. Esto que estás sintiendo necesita cuidado. Si hoy sientes que no puedes más, está bien pedir ayuda. ¿A quién podrías contactar ahora para que sepa cómo te sientes?"
    ),
    "sintoma_fisico" to listOf(
        "Gracias por compartir lo que está pasando con tu cuerpo. Si el malestar persiste, se intensifica o va acompañado de síntomas como fiebre, sangrado, debilidad o dolor fuerte, es importante consultar con un profesional de salud. ¿Hay alguien que pueda acompañarte a hacerlo?",
        "Tu cuerpo merece atención y cuidado. Lo que me cuentas es importante, y si el síntoma continúa o te preocupa, no dudes en buscar orientación médica. ¿Te sientes acompañada para poder hacerlo?",
        "Sé que el malestar físico puede ser abrumador. Si estás experimentando dolor, fiebre, sangrado o debilidad, es fundamental que recibas atención médica. ¿Hay alguien que pueda ayudarte a consultar con un profesional?"
    )
)

// ============================================================
// DATA CLASS
// ============================================================
data class BankItem(
    val id: String,
    val text: String,
    val question: String? = null,
    val family: String,
    val module: String,
    val requiredTags: List<String> = emptyList()
)

// ============================================================
// RESPONSE SELECTOR
// ============================================================
class ResponseSelector {
    companion object {
        fun getResponse(
            category: String,
            history: List<BankItem>,
            userName: String? = null
        ): String {
            val responses = STRUCTURED_RESPONSES[category] ?: STRUCTURED_RESPONSES["desconocido"]!!

            if (history.isEmpty()) {
                return getResponseByFamily(responses, "pregunta_directa", userName)
                    ?: getResponseByFamily(responses, "inicio", userName)
                    ?: responses.random().text.replace("{name}", userName ?: "")
            }

            val lastResponse = history.lastOrNull()
            val lastIsQuestion = lastResponse?.text?.endsWith("?") == true
            val lastIsInvitation = lastResponse?.text?.contains("puedes contarme") == true ||
                    lastResponse?.text?.contains("Si quieres") == true ||
                    lastResponse?.text?.contains("Si te parece") == true

            val recentQuestions = history.takeLast(3).count { it.text.endsWith("?") }

            val targetFamily = when {
                recentQuestions >= 2 -> "afirmacion_validante"
                lastIsQuestion -> "invitacion_suave"
                lastIsInvitation -> if (Random.nextBoolean()) "afirmacion_validante" else "pregunta_directa"
                else -> when (Random.nextInt(3)) {
                    0 -> "pregunta_directa"
                    1 -> "invitacion_suave"
                    else -> "afirmacion_validante"
                }
            }

            var response = getResponseByFamily(responses, targetFamily, userName)

            if (response == null) {
                response = responses.random().text.replace("{name}", userName ?: "")
            }

            return response
        }

        private fun getResponseByFamily(
            responses: List<BankItem>,
            family: String,
            userName: String?
        ): String? {
            val filtered = responses.filter { it.family == family }
            if (filtered.isEmpty()) return null
            return filtered.random().text.replace("{name}", userName ?: "")
        }

        fun getSaludo(name: String?): String {
            val saludos = DIRECT_RESPONSES["saludo"] ?: listOf("Hola{name}. ¿Cómo estás hoy?")
            return saludos.random().replace("{name}", name ?: "")
        }

        fun getCierre(): String {
            val cierres = DIRECT_RESPONSES["cierre"] ?: listOf("Gracias por compartir. Hasta luego.")
            return cierres.random()
        }

        fun getSafetyResponse(riskType: String): String {
            val responses = SAFETY_RESPONSES[riskType] ?: SAFETY_RESPONSES["riesgo_medio"]!!
            return responses.random()
        }
    }
}

// ============================================================
// STRUCTURED RESPONSES
// ============================================================
internal val STRUCTURED_RESPONSES: Map<String, List<BankItem>> = mapOf(

    // ============================================================
    // BIENESTAR — categoría nueva
    // ============================================================
    "bienestar" to listOf(
        BankItem(
            id = "bienestar_001",
            text = "Qué bueno escuchar eso. Los momentos de bienestar también merecen ser nombrados y disfrutados, sobre todo en esta etapa donde todo cambia tan rápido. ¿Hay algo en particular que esté contribuyendo a que te sientas así hoy?",
            question = "¿Hay algo en particular que esté contribuyendo a ese bienestar?",
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "bienestar_002",
            text = "Me alegra mucho. A veces las madres nos acostumbramos tanto a la exigencia que olvidamos registrar los ratos en que estamos bien. ¿Quieres contarme qué está haciendo posible ese bienestar?",
            question = "¿Quieres contarme qué está haciendo posible ese bienestar?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "bienestar_003",
            text = "Gracias por compartirlo. Reconocer los días buenos no es un lujo: es una forma de cuidar tu salud mental. ¿Cómo te gustaría sostener ese bienestar durante la semana?",
            question = "¿Cómo te gustaría sostener ese bienestar durante la semana?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "bienestar_004",
            text = "Qué lindo saber que hoy estás bien. Tu bienestar también es importante para tu bebé y para ti. ¿Qué te gustaría hacer con esta energía hoy?",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // MULTITEMAS
    // ============================================================
    "multitema_ambivalencia_sintoma_ansiedad" to listOf(
        BankItem(
            id = "multi_amb_sint_ans_001",
            text = "Te escucho con mucha atención. Estás tocando varios puntos importantes al mismo tiempo: un malestar físico, una inquietud emocional y una pregunta profunda sobre tu embarazo. Primero lo más urgente: si el síntoma físico continúa, empeora, o viene acompañado de dolor fuerte, fiebre, sangrado o debilidad, busca orientación de salud. Y también podemos mirar con calma la parte emocional. ¿Qué parte sientes que necesita más atención en este momento?",
            question = "¿Qué parte sientes que necesita más atención en este momento?",
            family = "integracion_sensible",
            module = "orientacion_salud"
        ),
        BankItem(
            id = "multi_amb_sint_ans_002",
            text = "Cuando el cuerpo se siente mal y la mente está inquieta, las dudas pueden hacerse más grandes. No voy a juzgarte ni a decirte qué hacer. Si el síntoma físico persiste o se vuelve intenso, buscar apoyo de salud es importante. Y emocionalmente, ¿qué parte está ocupando más espacio?",
            question = "¿Qué parte está ocupando más espacio?",
            family = "integracion_sensible",
            module = "orientacion_salud"
        )
    ),

    "multitema_sintoma_ansiedad" to listOf(
        BankItem(
            id = "multi_sint_ans_001",
            text = "Noto que el malestar físico y la ansiedad están apareciendo juntos. Esa combinación puede ser muy agotadora. Si el síntoma continúa o se intensifica con dolor, fiebre o sangrado, es importante buscar orientación de salud. Y emocionalmente, ¿qué es lo que más te preocupa de lo que estás sintiendo?",
            question = "¿Qué es lo que más te preocupa de lo que estás sintiendo?",
            family = "cuerpo_emocion",
            module = "orientacion_salud"
        ),
        BankItem(
            id = "multi_sint_ans_002",
            text = "El cuerpo y la mente están conectados. Cuando uno se siente incómodo, el otro suele acompañarlo. Si el malestar físico persiste o empeora, busca apoyo de salud. ¿Sientes que la ansiedad viene del síntoma físico o que el síntoma se intensifica cuando estás ansiosa?",
            question = "¿La ansiedad viene del síntoma o el síntoma se intensifica con la ansiedad?",
            family = "cuerpo_emocion",
            module = "orientacion_salud"
        )
    ),

    "multitema_tristeza_ansiedad" to listOf(
        BankItem(
            id = "multi_trist_ans_001",
            text = "Siento que estás llevando tristeza y ansiedad al mismo tiempo. Una parte quiere llorar, y la otra no para de pensar. Esa mezcla es agotadora. ¿Cuál de las dos está ocupando más espacio en ti ahora?",
            question = "¿Cuál está ocupando más espacio?",
            family = "emociones_mezcladas",
            module = "estado_emocional"
        ),
        BankItem(
            id = "multi_trist_ans_002",
            text = "Cuando la tristeza y la ansiedad van juntas, todo puede sentirse más pesado y acelerado a la vez. No tienes que resolverlo todo hoy. ¿Dirías que la tristeza viene más por algo que pasó o la ansiedad por algo que temes que pase?",
            question = "¿Viene más del pasado o del futuro?",
            family = "emociones_mezcladas",
            module = "estado_emocional"
        ),
        BankItem(
            id = "multi_trist_ans_003",
            text = "Tristeza y ansiedad juntas pueden dejarte muy cansada por dentro. Si pudiéramos separarlas un poco, ¿qué aparece primero: las ganas de llorar o los pensamientos que no paran?",
            question = "¿Qué aparece primero: el llanto o los pensamientos?",
            family = "emociones_mezcladas",
            module = "estado_emocional"
        )
    ),

    // ============================================================
    // SALUDO Y CIERRE
    // ============================================================
    "saludo" to listOf(
        BankItem(
            id = "saludo_001",
            text = "Hola{name}. Soy GestarBien. Estoy aquí para acompañarte con calma. Puedes contarme qué está pasando hoy.",
            question = "Puedes contarme qué está pasando hoy.",
            family = "inicio",
            module = "bienvenida"
        ),
        BankItem(
            id = "saludo_002",
            text = "Hola{name}. Gracias por estar aquí. Podemos ir paso a paso. ¿Cómo llegas hoy a este espacio?",
            question = "¿Cómo llegas hoy a este espacio?",
            family = "inicio",
            module = "bienvenida"
        ),
        BankItem(
            id = "saludo_003",
            text = "Hola{name}. Este es un espacio para escucharte sin juzgarte. ¿Qué te gustaría contarme primero?",
            question = "¿Qué te gustaría contarme primero?",
            family = "inicio",
            module = "bienvenida"
        )
    ),

    "cierre" to listOf(
        BankItem(
            id = "cierre_001",
            text = "Gracias por compartir este momento conmigo. Puedes volver cuando lo necesites, aquí estaré.",
            question = null,
            family = "cierre_suave",
            module = "cierre"
        ),
        BankItem(
            id = "cierre_002",
            text = "Ha sido un privilegio acompañarte. Cuida de ti hasta la próxima.",
            question = null,
            family = "cierre_suave",
            module = "cierre"
        ),
        BankItem(
            id = "cierre_003",
            text = "Cerramos por ahora. Lo que compartiste es importante. Hasta luego.",
            question = null,
            family = "cierre_suave",
            module = "cierre"
        )
    ),

    // ============================================================
    // MALESTAR
    // ============================================================
    "malestar" to listOf(
        BankItem(
            id = "malestar_001",
            text = "Cuando dices que te sientes mal, a veces se mezclan muchas cosas. ¿Sientes que ese malestar está más en tu cuerpo, en tus pensamientos o en tus emociones?",
            question = "¿Ese malestar es más físico, mental o emocional?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_002",
            text = "Siento que hoy no está siendo un día fácil. No tienes que buscar las palabras perfectas. ¿Te sientes más cansada, triste o abrumada?",
            question = "¿Te sientes más cansada, triste o abrumada?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_003",
            text = "A veces el malestar aparece de repente o va creciendo despacio. ¿Recuerdas en qué momento del día empezaste a sentirte así?",
            question = "¿En qué momento del día empezó el malestar?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_004",
            text = "Siento mucho que las cosas se sientan tan pesadas hoy. ¿Estás sola en este momento o hay alguien cerca a quien puedas acudir?",
            question = "¿Estás sola o acompañada en este momento?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_005",
            text = "En otros momentos difíciles, ¿hay alguna pequeña cosa que te haya ayudado a sentir un poco de alivio o tranquilidad?",
            question = "¿Qué te ha dado un poco de alivio antes?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_006",
            text = "Hay malestares que se pueden sostener y otros que sobrepasan. ¿Sientes que este malestar es algo que puedes llevar ahora o te supera?",
            question = "¿Sientes que el malestar te sobrepasa en este momento?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_007",
            text = "A veces el malestar es la forma en que el cuerpo o la mente piden una pausa. Si pudieras pedir algo en este instante, ¿qué sería?",
            question = "¿Si pudieras pedir algo ahora, qué sería?",
            family = "invitacion_suave",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_008",
            text = "Cargar con todo sin pausa agota a cualquiera. Si pudieras sacar una sola cosa de esa mochila pesada que llevas hoy, ¿cuál sería?",
            question = "¿Qué es lo que más te pesa hoy?",
            family = "invitacion_suave",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_009",
            text = "Entre tantas cosas que atiendes a diario, ¿qué parte de ti siente que necesita un poco más de pausa y cuidado hoy?",
            question = "¿Qué parte de ti necesita más cuidado hoy?",
            family = "invitacion_suave",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_010",
            text = "A veces el malestar nos avisa que hemos llegado a nuestro límite. ¿Has podido comer, tomar agua o descansar un poco hoy?",
            question = "¿Has podido atender tus necesidades básicas hoy?",
            family = "pregunta_directa",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_011",
            text = "Te escucho. Puedes quedarte aquí el tiempo que necesites.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_012",
            text = "No tienes que resolver nada en este momento. Solo estar aquí, respirar y sostener lo que sientes, ya es más que suficiente.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_013",
            text = "Ese malestar tiene un lugar aquí. No necesitas justificarlo ni explicarlo.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_014",
            text = "Estoy aquí. Sin prisas, sin exigencias.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_015",
            text = "No hay formas correctas o incorrectas de sentirse. Aquí no hay expectativas que cumplir.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_016",
            text = "A veces el malestar llega sin nombre, y eso también merece cuidado.",
            question = null,
            family = "reflexion",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_017",
            text = "No saber exactamente qué está pasando no significa que estés exagerando.",
            question = null,
            family = "reflexion",
            module = "estado_emocional"
        ),
        BankItem(
            id = "malestar_018",
            text = "El malestar suele funcionar como una marea: sube, alcanza un punto alto y luego empieza a ceder. No se quedará así para siempre.",
            question = null,
            family = "reflexion",
            module = "estado_emocional"
        )
    ),

    // ============================================================
    // TRISTEZA
    // ============================================================
    "tristeza" to listOf(
        BankItem(
            id = "tristeza_gen_001",
            text = "Siento mucho que hoy estés pasando por esto. La tristeza puede hacer que todo se sienta pesado y cuesta arriba. ¿Hay algo en particular que te esté doliendo o es un sentimiento que llegó sin avisar?",
            question = "¿Hay un detonante claro o llegó sin avisar?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_002",
            text = "Siento verte triste. No tienes que fingir estar bien aquí. Si pudieras pedir un pequeño alivio en este instante, ¿qué sería?",
            question = "¿Qué te gustaría recibir o sentir en este momento?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_003",
            text = "Cuando la tristeza se instala, suele traer consigo muchas preocupaciones acumuladas. ¿Qué es lo que más te está pesando en este instante?",
            question = "¿Qué es lo que más te está pesando ahora?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_004",
            text = "A veces la tristeza se intensifica a ciertas horas o en situaciones específicas. ¿En qué momentos del día notas que llega con más fuerza?",
            question = "¿En qué momentos del día se intensifica?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_005",
            text = "Atravesar la tristeza puede ser agotador. ¿Has podido descansar, comer o tomar un poco de agua hoy?",
            question = "¿Has podido atender tus necesidades básicas hoy?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_006",
            text = "Permitirte sentir tristeza y expresarla no es una debilidad; es una forma sana de liberar lo que llevas dentro.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_007",
            text = "Puedes quedarte aquí con lo que sientes todo el tiempo que necesites. No hay prisa por 'estar bien'.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_008",
            text = "No tienes que sonreír ni disimular aquí. Tu tristeza es completamente válida y tiene un lugar seguro.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_009",
            text = "La tristeza es una emoción que te está visitando en este momento, pero no define quién eres. Puedes dejarla estar sin que se quede para siempre.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_010",
            text = "La tristeza también es una respuesta natural del cuerpo y la mente cuando atravesamos grandes transformaciones y cambios de vida.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_gen_011",
            text = "Estar triste no destruye tu capacidad de cuidar ni tu fortaleza. Solo muestra que necesitas un momento de pausa y autocompasión.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "tristeza_tag_mala_madre_001",
            text = "Escuchar esa voz interna que te dice que eres 'mala madre' o que estás fallando es agotador. Sentirte triste o desbordada no te hace una mala madre, te hace humana.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("mala_madre")
        ),
        BankItem(
            id = "tristeza_tag_mala_madre_002",
            text = "Que te sientas triste o dudosa no cambia el amor ni el cuidado que tienes por tu bebé. Quítate la exigencia de ser perfecta por un momento. ¿Qué necesitarías escuchar ahora para sentirte en paz?",
            question = "¿Qué necesitarías escuchar ahora para sentirte en paz?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("mala_madre")
        ),
        BankItem(
            id = "tristeza_tag_mala_madre_003",
            text = "Exigirte estar feliz todo el tiempo es una carga insostenible. ¿Sientes que esta presión viene de ti misma o de lo que otros esperan de ti?",
            question = "¿Sientes que esa presión viene de ti o del entorno?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = listOf("mala_madre")
        ),
        BankItem(
            id = "tristeza_tag_culpa_001",
            text = "La culpa suele aparecer cuando nos exigimos estándares imposibles en medio del cansancio. No estás haciendo nada mal por sentir lo que sientes hoy.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("culpa")
        ),
        BankItem(
            id = "tristeza_tag_culpa_002",
            text = "Sentir culpa por estar triste es una carga doblemente pesada. Si una amiga muy querida estuviera pasando por lo mismo, ¿le dirías que es su culpa?",
            question = "¿Serías tan dura con una amiga como lo estás siendo contigo?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("culpa")
        ),
        BankItem(
            id = "tristeza_tag_culpa_003",
            text = "La culpa intenta convencerte de que debiste hacer más, pero la realidad es que estás haciendo lo mejor que puedes con la energía que tienes hoy.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = listOf("culpa")
        ),
        BankItem(
            id = "tristeza_tag_soledad_001",
            text = "Sentir tristeza en soledad puede hacer que todo parezca más oscuro de lo que es. Aunque estemos a través de una pantalla, aquí tienes un espacio de compañía sin juicios.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("soledad")
        ),
        BankItem(
            id = "tristeza_tag_soledad_002",
            text = "Nadie debería atravesar estos momentos en absoluto aislamiento. ¿Hay alguna persona de confianza a la que puedas escribirle un mensaje hoy, aunque sea corto?",
            question = "¿Tienes a alguien a quien puedas escribirle hoy?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = listOf("soledad")
        ),
        BankItem(
            id = "tristeza_tag_llanto_001",
            text = "Llorar es la forma que tiene el cuerpo de soltar la tensión acumulada. No intentes contener las lágrimas si necesitan salir; déjalas fluir.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("llanto")
        ),
        BankItem(
            id = "tristeza_tag_llanto_002",
            text = "Está bien desahogarse y dejar que salgan las lágrimas. Si lo necesitas, tómate un momento, busca un vaso de agua y regresa cuando quieras hablar.",
            question = "¿Te gustaría tomarte una pausa o prefieres desahogarte aquí?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("llanto")
        )
    ),

    // ============================================================
    // ANSIEDAD
    // ============================================================
    "ansiedad" to listOf(
        BankItem(
            id = "ansiedad_gen_001",
            text = "Esa ansiedad parece estar haciendo mucho ruido por dentro. No tenemos que resolver todo ahora. ¿Hay algún pensamiento en particular que se esté repitiendo sin parar?",
            question = "¿Qué pensamiento se repite más?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_002",
            text = "La ansiedad puede sentirse como una alarma encendida en todo el cuerpo. En este momento, ¿sientes esa inquietud más en la mente o en la parte física?",
            question = "¿Se siente más en el cuerpo o en la mente?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_003",
            text = "A veces la ansiedad llega de golpe o se acumula poco a poco. ¿Hay algún momento del día en que notes que se intensifica más?",
            question = "¿En qué momentos del día aparece con más fuerza?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_004",
            text = "Sé que la ansiedad se siente agotadora. Si pudieras hacer algo muy pequeño por ti en este instante, como tomar un sorbo de agua o cambiar de posición, ¿qué te vendría bien?",
            question = "¿Qué pequeño gesto te daría un poco de alivio?",
            family = "invitacion_suave",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_005",
            text = "Tu mente o tu cuerpo pueden estar acelerados en este momento, pero aquí no hay prisa. Estamos en un espacio seguro.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_006",
            text = "No tienes que luchar contra esa ansiedad ni forzarte a 'calmarte' de inmediato. Solo nombrarla y reconocerla ya es un gran paso.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_007",
            text = "Estar ansiosa no significa que estés haciendo algo mal o que vaya a pasar algo malo. Es solo tu sistema nervioso buscando protegerte.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_008",
            text = "La ansiedad exagera las amenazas y reduce nuestra percepción de fortaleza. Recuerda que no tienes que resolver el mañana hoy.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_gen_009",
            text = "La ansiedad es como una ola: sube, alcanza su punto más alto y luego vuelve a bajar. Puedes dejarla pasar sin engancharte en ella.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_tag_parto_001",
            text = "El miedo al parto o a la salud del bebé es una de las ansiedades más comunes y profundas durante la gestación. Es completamente normal sentir incertidumbre ante un evento tan grande.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("miedo_parto")
        ),
        BankItem(
            id = "ansiedad_tag_parto_002",
            text = "A veces nos llenamos de historias o información que alimentan el temor al parto. ¿Hay alguna duda específica que te esté quitando la tranquilidad últimamente?",
            question = "¿Qué es lo que más te preocupa sobre ese momento?",
            family = "invitacion_suave",
            module = "externalizacion",
            requiredTags = listOf("miedo_parto")
        ),
        BankItem(
            id = "ansiedad_tag_parto_003",
            text = "Tu cuerpo sabe atravesar este proceso y tendrás acompañamiento profesional. Concentrémonos en el día de hoy, un paso a la vez.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = listOf("miedo_parto")
        ),
        BankItem(
            id = "ansiedad_tag_futuro_001",
            text = "Cuando la mente viaja al futuro intenta controlarlo todo, y eso genera un cansancio enorme. Trata de volver al presente: ¿dónde estás sentada o apoyada en este momento?",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("futuro")
        ),
        BankItem(
            id = "ansiedad_tag_futuro_002",
            text = "Intentar resolver problemas que aún no han ocurrido agota tus energías de hoy. ¿Hay algo de todo lo que piensas que realmente puedas resolver hoy?",
            question = "¿Hay algo que esté a tu alcance resolver hoy?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = listOf("futuro")
        ),
        BankItem(
            id = "ansiedad_tag_fisico_001",
            text = "La presión en el pecho, la respiración corta o la tensión son formas en que el cuerpo descarga la ansiedad. No estás en peligro, tu cuerpo solo necesita un momento de pausa.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("sintomas_fisicos")
        ),
        BankItem(
            id = "ansiedad_tag_fisico_002",
            text = "Siente el apoyo de tus pies en el suelo o la espalda sobre la silla. Inhala despacio por la nariz y suelta el aire lentamente por la boca. ¿Cómo sientes tu respiración ahora?",
            question = "¿Quieres que hagamos un ejercicio breve de respiración?",
            family = "invitacion_suave",
            module = "externalizacion",
            requiredTags = listOf("sintomas_fisicos")
        )
    ),

    // ============================================================
    // ANSIEDAD POR EL BEBÉ
    // ============================================================
    "ansiedad_bebe" to listOf(
        BankItem(
            id = "ansiedad_bebe_gen_001",
            text = "Preocuparte por el bienestar de tu bebé es una respuesta natural de tu instinto protector. A veces, la mente exagera las amenazas para intentar mantenerlo a salvo. ¿Qué es lo que más te está inquietando en este instante?",
            question = "¿Qué es lo que más te inquieta sobre tu bebé ahora?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_gen_002",
            text = "La ansiedad por el bebé puede hacer que sientas que todo está fuera de control. Si pudieras darte una pausa de dos minutos, ¿qué necesitarías para sentir un poco de calma?",
            question = "¿Qué te ayudaría a sentir un poco más de calma?",
            family = "invitacion_suave",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_gen_003",
            text = "Tener pensamientos o miedos intensos sobre tu bebé no significa que seas una mala madre ni que algo vaya a salir mal. Es solo tu cerebro en estado de alerta máxima.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_gen_004",
            text = "Es muy agotador estar vigilando constantemente que todo esté bien. Tu amor por tu bebé es enorme y es comprensible que sientas este peso.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_gen_005",
            text = "Pensar en un escenario feo no hace que ocurra. Tu mente está intentando proteger a tu bebé, pero no todos los pensamientos de miedo son predicciones reales.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_gen_006",
            text = "Cuidar a tu bebé también implica cuidar de ti. Soltar la preocupación por un momento no te hace descuidada; te permite recuperar energía.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ansiedad_bebe_tag_salud_001",
            text = "Cuando notamos cualquier cambio en la salud o el cuerpo del bebé, es normal que la alarma interna se encienda. Si sientes que hay signos médicos de alerta, consultar con su pediatra te dará la certeza que necesitas.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("salud_bebe")
        ),
        BankItem(
            id = "ansiedad_bebe_tag_salud_002",
            text = "Frente a los síntomas de tu bebé, es muy fácil sobrepensar el peor escenario. ¿Es una duda que puedes consultar con tu médico o es un miedo difuso?",
            question = "¿Crees necesario consultar con el pediatra para quedarte tranquila?",
            family = "pregunta_directa",
            module = "externalizacion",
            requiredTags = listOf("salud_bebe")
        ),
        BankItem(
            id = "ansiedad_bebe_tag_vigilancia_001",
            text = "El impulso de revisar constantemente al bebé o ver si respira es muy común en el posparto, pero puede privarte del descanso que desesperadamente necesitas.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("hipervigilancia")
        ),
        BankItem(
            id = "ansiedad_bebe_tag_vigilancia_002",
            text = "Estar en guardia constante agota el cuerpo y la mente. ¿Hay alguien que pueda velar por el bebé un rato mientras tú descansas o te das una ducha?",
            question = "¿Tienes a alguien que te tome el relevo un momento?",
            family = "invitacion_suave",
            module = "externalizacion",
            requiredTags = listOf("hipervigilancia")
        ),
        BankItem(
            id = "ansiedad_bebe_tag_miedo_001",
            text = "Los pensamientos intrusivos sobre que le pase algo terrible a tu bebé son espantosos, pero son solo eso: pensamientos involuntarios provocados por la ansiedad, no deseos ni corazonadas.",
            question = null,
            family = "reflexion",
            module = "externalizacion",
            requiredTags = listOf("miedo_perderlo")
        ),
        BankItem(
            id = "ansiedad_bebe_tag_miedo_002",
            text = "Sentir ese miedo tan profundo muestra lo valioso que es tu bebé para ti. Cuando llegue esa imagen de susto, inhala profundo y recuerda: 'Mi bebé está bien ahora mismo y estamos a salvo'.",
            question = null,
            family = "afirmacion_validante",
            module = "externalizacion",
            requiredTags = listOf("miedo_perderlo")
        )
    ),

    // ============================================================
    // CANSANCIO
    // ============================================================
    "cansancio" to listOf(
        BankItem(
            id = "cansancio_gen_001",
            text = "Ese cansancio suena profundo y acumulado. A veces no es solo la falta de sueño, sino el peso de sostener tanto todo el tiempo. ¿Ese agotamiento lo sientes más en el cuerpo, en la cabeza o en ambos?",
            question = "¿Se siente más en el cuerpo, en la mente o en ambos?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_gen_002",
            text = "Estar exhausta no significa que no puedas con esto ni que estés fallando; solo significa que has estado dando demasiado durante mucho tiempo. Si pudieras pausar algo por hoy, ¿qué soltarías?",
            question = "¿Hay alguna tarea que pudieras soltar o delegar hoy?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_gen_003",
            text = "El cansancio en esta etapa es real, intenso y totalmente válido. No tienes que justificar tu agotamiento ni disculparte por no dar el cien por ciento hoy.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_gen_004",
            text = "Llevar la rutina del día a día cuando el cuerpo pide a gritos un descanso requiere un enorme esfuerzo. Permítete hacer solo lo mínimo indispensable hoy.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_gen_005",
            text = "Descansar no es un premio que ganas después de dejar todo perfecto; es una necesidad básica para sostenerte a ti y a tu bebé.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_gen_006",
            text = "A veces el cuerpo nos frena a través del cansancio para obligarnos a mirar nuestras propias necesidades. Parar también es cuidar.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cansancio_tag_sueno_001",
            text = "Las noches fraccionadas y la privación de sueño son de las pruebas más duras de la maternidad. La falta de descanso continuo altera el ánimo y hace que todo se sienta el doble de difícil.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("falta_sueno")
        ),
        BankItem(
            id = "cansancio_tag_sueno_002",
            text = "Acumular horas sin dormir agota hasta la última reserva de paciencia. Si el bebé llega a dormirse una siesta hoy, ¿tienes la posibilidad de acostarte tú también aunque sea 30 minutos?",
            question = "¿Podrías intentar acostarte un rato hoy si se da la oportunidad?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("falta_sueno")
        ),
        BankItem(
            id = "cansancio_tag_carga_001",
            text = "Cuidar a un bebé, atender la casa y sostener las responsabilidades diarias no es trabajo de una sola persona. La carga mental de coordinarlo todo suele ser más agotadora que el trabajo físico.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("sobrecarga")
        ),
        BankItem(
            id = "cansancio_tag_carga_002",
            text = "Hacerte cargo de todo sin pausa te llevará al límite rápidamente. ¿Hay alguna tarea doméstica o pendiente que puedas dejar intencionalmente sin hacer hoy sin que pase nada grave?",
            question = "¿Qué pendiente podrías aplazar o dejar de lado por hoy?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = listOf("sobrecarga")
        ),
        BankItem(
            id = "cansancio_tag_burnout_001",
            text = "Llegar al límite y sentir que 'ya no puedes más' es una señal de auxilio del cuerpo y la mente, no un fallo personal. Has estado sosteniendo más de lo que a una sola persona le corresponde.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("burnout_maternal")
        ),
        BankItem(
            id = "cansancio_tag_burnout_002",
            text = "Estar al borde del colapso requiere pedir un relevo de inmediato. ¿Tienes a alguien cercano (pareja, familiar, amiga) a quien decirle concretamente 'necesito que te hagas cargo un rato'?",
            question = "¿Puedes pedir un relevo urgente a alguien de tu entorno?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = listOf("burnout_maternal")
        )
    ),

    // ============================================================
    // FRUSTRACIÓN
    // ============================================================
    "frustracion" to listOf(
        BankItem(
            id = "frustracion_gen_001",
            text = "La frustración suele aparecer cuando hemos estado acumulando presión durante mucho tiempo y las cosas no salen como necesitamos. ¿Qué fue lo que desencadenó esa sensación hoy?",
            question = "¿Qué fue lo que más te frustró hoy?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_gen_002",
            text = "Parece que vienes intentando sostenerlo todo con paciencia, pero ya llegaste a un límite. ¿Sientes que hay algo o alguien en particular que te esté exigiendo de más?",
            question = "¿Sientes que hay una exigencia desmedida sobre ti?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_gen_003",
            text = "A veces la frustración es una señal de que necesitamos poner un freno o marcar un espacio personal. Si pudieras cambiar una sola cosa de tu día en este instante, ¿qué sería?",
            question = "¿Qué pequeño cambio te daría un respiro ahora?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_gen_004",
            text = "Es completamente válido sentir rabia o frustración. No tienes que ser una fuente inagotable de paciencia ni mantener la calma todo el tiempo.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_gen_005",
            text = "Sentir que las cosas se salen de tus manos no te convierte en una mala madre ni en alguien incapaz; solo demuestra que estás exhausta.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_gen_006",
            text = "La frustración no es un fallo personal, es el choque entre la energía que tienes y lo que el entorno te exige. Reconocerla es el primer paso para protegerte.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "frustracion_tag_llanto_001",
            text = "El llanto prolongado del bebé activa una respuesta de estrés muy intensa en el cerebro maternal. Es normal sentir frustración o desespero cuando intentas de todo y nada parece calmarlo.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("llanto_bebe")
        ),
        BankItem(
            id = "frustracion_tag_llanto_002",
            text = "Si sientes que el desespero te desborda, deja al bebé en un lugar seguro (como su cuna), da tres pasos atrás y respira profundo un minuto. Su seguridad y tu calma van de la mano. ¿Hay alguien que pueda tomar el relevo un momento?",
            question = "¿Puedes dejar al bebé en un sitio seguro y tomarte un minuto de aire?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("llanto_bebe")
        ),
        BankItem(
            id = "frustracion_tag_expectativa_001",
            text = "Con un bebé, los planes estructurados suelen desbaratarse en minutos. Ajustar las expectativas a la realidad del día a día no es rendirse, es evitar un desgaste innecesario.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = listOf("expectativa_vs_realidad")
        ),
        BankItem(
            id = "frustracion_tag_expectativa_002",
            text = "Querías avanzar con tus cosas y el día tomó otro rumbo. Es frustrante, pero no es tu culpa. ¿Puedes soltar la lista de pendientes por lo que resta de día?",
            question = "¿Podrías darte permiso de pausar la lista de pendientes por hoy?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("expectativa_vs_realidad")
        ),
        BankItem(
            id = "frustracion_tag_control_001",
            text = "La impotencia aparece cuando queremos cambiar una situación pero no está en nuestras manos arreglarla de inmediato. Acepta que hoy se hace lo que se puede.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("sin_control")
        )
    ),

    // ============================================================
    // CONFUSIÓN
    // ============================================================
    "confusion" to listOf(
        BankItem(
            id = "confusion_gen_001",
            text = "Vamos despacio. Cuando hay demasiada información o emociones mezcladas, tomar cualquier decisión se siente abrumador. ¿Qué pensamiento o duda es la que más vueltas te da ahora?",
            question = "¿Qué duda es la que más vueltas te está dando?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "confusion_gen_002",
            text = "La confusión no significa que estés perdida o haciendo las cosas mal; solo indica que necesitas pausar y filtrar lo que está pasando. ¿Esa duda viene de algo tuyo o de lo que dicen los demás?",
            question = "¿Esa duda nace de ti o del entorno?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "confusion_gen_003",
            text = "No tienes por qué tener todas las respuestas resueltas el día de hoy. Está bien decir 'no sé qué hacer en este momento' y dárselo un tiempo.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "confusion_gen_004",
            text = "Es totalmente normal dudar en una etapa donde todo está cambiando tan rápido. Despejar la mente toma tiempo, no te presiones.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "confusion_gen_005",
            text = "Cuando hay ruido alrededor, tu propia voz e intuición se apagan. Antes de buscar respuestas afuera, intenta escuchar lo que tú necesitas.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "confusion_tag_opiniones_001",
            text = "Recibir comentarios contradictorios de familiares, conocidos o internet crea un ruido enorme. Recuerda que la experta en tu bebé y en tu vida eres tú.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("exceso_consejos")
        ),
        BankItem(
            id = "confusion_tag_opiniones_002",
            text = "Todo el mundo parece tener una opinión sobre cómo debes criar o cuidarte. ¿Te ayudaría silenciar por un momento los consejos externos y conectar con lo que a ti te hace sentido?",
            question = "¿Te serviría hacer una pausa de los consejos ajenos?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("exceso_consejos")
        ),
        BankItem(
            id = "confusion_tag_niebla_001",
            text = "La 'niebla mental' o los olvidos frecuentes en la gestación y posparto son producto de las fluctuaciones hormonales y el cansancio acumulado. Tu cerebro está reorganizándose, no te estás volviendo loca.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("niebla_mental")
        ),
        BankItem(
            id = "confusion_tag_niebla_002",
            text = "Intenta no confiarle todo a la memoria por ahora; anota en una libreta o el celular solo lo imprescindible y suelta lo demás.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = listOf("niebla_mental")
        ),
        BankItem(
            id = "confusion_tag_decision_001",
            text = "Frente a una decisión importante sobre la crianza o tu salud, tómate el tiempo de evaluar solo una opción a la vez. No hay elecciones perfectas, solo caminos posibles.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = listOf("incertidumbre_decisiones")
        ),
        BankItem(
            id = "confusion_tag_decision_002",
            text = "Si la decisión no es urgente, ¿te parece si la dejamos en pausa por unas horas para permitir que tu mente se despeje?",
            question = "¿Podrías posponer esa decisión hasta estar más descansada?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("incertidumbre_decisiones")
        )
    ),

    // ============================================================
    // AMBIVALENCIA
    // ============================================================
    "ambivalencia_embarazo" to listOf(
        BankItem(
            id = "ambivalencia_gen_001",
            text = "No eres una mala persona ni una mala madre por sentir o pensar esto. Tener dudas o sentimientos encontrados sobre el embarazo es más común de lo que se habla. ¿Esta sensación aparece más cuando estás exhausta, sola o bajo mucha presión?",
            question = "¿Aparece más cuando estás sola, cansada o presionada?",
            family = "pregunta_directa",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_gen_002",
            text = "Una duda sobre tu maternidad merece contención y escucha, nunca juicios ni reproches. Al mirar lo que sientes hoy, ¿qué crees que pesa más: el miedo, el cansancio o la falta de apoyo?",
            question = "¿Qué crees que pesa más hoy: miedo, cansancio o falta de apoyo?",
            family = "pregunta_directa",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_gen_003",
            text = "A veces las dudas son la forma en que el cuerpo y la mente dicen 'necesito descanso', 'necesito ayuda' o 'me da miedo el futuro'. ¿Cuál de esas necesidades se acerca más a lo que estás viviendo?",
            question = "¿Qué necesidad profunda sientes que hay detrás de esta duda?",
            family = "invitacion_suave",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_gen_004",
            text = "Puedes amar a tu bebé y, al mismo tiempo, sentirte abrumada o asustada por todo lo que implica la maternidad. Ambos sentimientos pueden convivir sin cancelarse.",
            question = null,
            family = "afirmacion_validante",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_gen_005",
            text = "Este es un espacio completamente seguro. Expresar lo difícil o confuso que se siente el proceso no te define ni destruye el amor que le tienes a tu hijo.",
            question = null,
            family = "afirmacion_validante",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_gen_006",
            text = "La maternidad real no se parece a las expectativas perfectas. Dudar o sentir rechazo en momentos de agotamiento no es falta de amor, es una reacción al exceso de carga.",
            question = null,
            family = "reflexion",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_tag_arrepentimiento_001",
            text = "Pensar 'desearía no haber quedado embarazada' o 'quiero mi vida de antes' en un momento de crisis no significa que seas una mala persona. Es la mente buscando una vía de escape al dolor o la presión actual.",
            question = null,
            family = "afirmacion_validante",
            module = "exploracion_segura",
            requiredTags = listOf("arrepentimiento_temporal")
        ),
        BankItem(
            id = "ambivalencia_tag_arrepentimiento_002",
            text = "Sentir arrepentimiento no es un punto de no retorno, es la señal de que la situación actual te supera. ¿Hay algún aspecto concreto de tu rutina que sientas que no puedes sostener más?",
            question = "¿Hay algo específico de tu rutina que sientas insostenible?",
            family = "pregunta_directa",
            module = "exploracion_segura",
            requiredTags = listOf("arrepentimiento_temporal")
        ),
        BankItem(
            id = "ambivalencia_tag_vida_anterior_001",
            text = "Extrañar a la persona que eras antes del embarazo o extrañar tu independencia es un proceso de duelo perfectamente válido. La llegada de un bebé transforma toda tu identidad.",
            question = null,
            family = "afirmacion_validante",
            module = "exploracion_segura",
            requiredTags = listOf("extrana_vida_previa")
        ),
        BankItem(
            id = "ambivalencia_tag_vida_anterior_002",
            text = "Echar de menos tu tiempo a solas, tus proyectos o tu autonomía no significa que no quieras a tu bebé. ¿Qué espacio o actividad personal de antes extrañas con más fuerza?",
            question = "¿Qué parte de tu vida previa extrañas más?",
            family = "invitacion_suave",
            module = "exploracion_segura",
            requiredTags = listOf("extrana_vida_previa")
        ),
        BankItem(
            id = "ambivalencia_tag_cambio_001",
            text = "Asumir la maternidad es una transición enorme. Adaptarse a esta nueva versión de ti toma tiempo, paciencia y autocompasión.",
            question = null,
            family = "reflexion",
            module = "exploracion_segura",
            requiredTags = listOf("miedo_al_cambio")
        )
    ),

    "ambivalencia_followup" to listOf(
        BankItem(
            id = "ambivalencia_followup_gen_001",
            text = "Podemos quedarnos en esta conversación todo el tiempo que necesites. Entender lo que sientes no requiere prisa. ¿Te ayuda seguir desahogándote por aquí o prefieres hacer una pausa?",
            question = "¿Prefieres seguir profundizando o te vendría bien una pausa?",
            family = "invitacion_suave",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_followup_gen_002",
            text = "Atravesar estas dudas en silencio suele ser muy pesado. Además de este espacio, ¿tienes a alguna persona de tu entera confianza a la que le puedas hablar francamente de esto sin sentirte juzgada?",
            question = "¿Tienes a alguien de confianza a quien contarle esto sin filtro?",
            family = "pregunta_directa",
            module = "exploracion_segura",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "ambivalencia_followup_gen_003",
            text = "Hablar de estos temas tabú demuestra mucha valentía y honestidad contigo misma. Poco a poco las emociones se irán acomodando.",
            question = null,
            family = "afirmacion_validante",
            module = "exploracion_segura",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // SÍNTOMA FÍSICO
    // ============================================================
    "sintoma_fisico" to listOf(
        BankItem(
            id = "sintoma_gen_001",
            text = "Escuchar al cuerpo es fundamental en esta etapa. Si este malestar es constante, se intensifica o te genera intranquilidad, es muy importante pedir una valoración médica. ¿Puedes contarme un poco más sobre lo que estás sintiendo?",
            question = "¿Puedes describir qué tipo de malestar estás sintiendo?",
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "sintoma_gen_002",
            text = "Tu cuerpo está atravesando transformaciones físicas enormes. Aunque muchas molestias son comunes, ninguna debe pasarse por alto si te causa dolor o alarma. ¿Cuentas con alguien que pueda acompañarte a una consulta si fuera necesario?",
            question = "¿Hay alguien que pueda acompañarte a consultar?",
            family = "pregunta_directa",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "sintoma_gen_003",
            text = "Cuidar de ti también significa atender las señales físicas sin minimizar tu dolor o incomodidad. No dudes en acudir a un profesional de salud para estar tranquila.",
            question = null,
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "sintoma_tag_alarma_001",
            text = "Síntomas como sangrado, fiebre alta, dolor abdominal intenso, pérdida de líquido o falta de movimiento del bebé son signos de alarma que requieren evaluación médica inmediata en urgencias. Por favor, no esperes y busca ayuda médica ahora.",
            question = "¿Tienes acceso a un centro de salud o urgencias cercano en este momento?",
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = listOf("signos_alarma")
        ),
        BankItem(
            id = "sintoma_tag_alarma_002",
            text = "Por tu seguridad y la de tu bebé, este tipo de síntoma necesita ser revisado presencialmente por un médico lo antes posible. Avisa a algún familiar o persona cercana para que te acompañe.",
            question = null,
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = listOf("signos_alarma")
        ),
        BankItem(
            id = "sintoma_tag_preeclampsia_001",
            text = "Un dolor de cabeza muy fuerte, ver luces, escuchar zumbidos o notar una hinchazón repentina en cara y manos son síntomas que exigen revisar tu presión arterial de inmediato. Acude a urgencias para una valoración.",
            question = null,
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = listOf("preeclampsia_alert")
        ),
        BankItem(
            id = "sintoma_tag_digestivo_001",
            text = "Las náuseas, el reflujo o el malestar estomacal son muy frecuentes, pero pueden ser agotadores. Si no logras retener líquidos o comida durante el día, debes consultar con tu médico para evitar deshidratarte.",
            question = "¿Logras tolerar agua o alimentos en este momento?",
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = listOf("malestar_digestivo")
        )
    ),

    "sintoma_fisico_followup" to listOf(
        BankItem(
            id = "sintoma_followup_gen_001",
            text = "Recuerda que ante cualquier duda o empeoramiento de tus síntomas, la consulta médica presencial es la mejor opción. ¿Cómo te sientes físicamente en este preciso instante?",
            question = "¿Cómo te sientes físicamente en este preciso instante?",
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "sintoma_followup_gen_002",
            text = "Seguir atenta a las señales de tu cuerpo es un acto de autocuidado. Si necesitas desahogar la carga emocional que este malestar te produce, aquí sigo.",
            question = "¿Hay algo más respecto a tu salud física o emocional que quieras compartir?",
            family = "invitacion_suave",
            module = "orientacion_salud",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // LACTANCIA
    // ============================================================
    "lactancia" to listOf(
        BankItem(
            id = "lactancia_gen_001",
            text = "La lactancia suele presentarse como algo 'natural' e intuitivo, pero en la realidad puede ser compleja, agotadora y llena de retos. ¿Qué aspecto de la lactancia te está resultando más difícil o abrumador hoy?",
            question = "¿Qué es lo que más te preocupa o agota de la lactancia ahora?",
            family = "pregunta_directa",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "lactancia_gen_002",
            text = "La forma en que decidas o puedas alimentar a tu bebé no define tu valor ni tu amor como madre. Hay muchas maneras válidas y amorosas de nutrir y construir un vínculo seguro.",
            question = null,
            family = "afirmacion_validante",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "lactancia_gen_003",
            text = "Sentir frustración, cansancio o incluso deseos de pausar o dejar la lactancia es totalmente válido. Tu bienestar mental y emocional es indispensable para el bienestar de tu bebé.",
            question = null,
            family = "reflexion",
            module = "orientacion_salud",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "lactancia_tag_dolor_001",
            text = "Amamantar no debería doler de forma insoportable. Si hay dolor agudo, grietas, ardor o zonas rojas y calientes con fiebre, es importante consultar con una asesora de lactancia o médico para descartar una mastitis o ajustar el agarre.",
            question = "¿Sientes fiebre, enrojecimiento o dolor punzante en el pecho?",
            family = "orientacion_salud",
            module = "orientacion_salud",
            requiredTags = listOf("dolor_lactancia")
        ),
        BankItem(
            id = "lactancia_tag_culpa_001",
            text = "Elegir la fórmula, combinarla o decidir un destete no es fallar ni rendirse; es cuidar la salud mental de ambos. Un bebé necesita, por encima de todo, una mamá tranquila y presente.",
            question = null,
            family = "afirmacion_validante",
            module = "orientacion_salud",
            requiredTags = listOf("culpa_formula_destete")
        ),
        BankItem(
            id = "lactancia_tag_culpa_002",
            text = "La presión social alrededor de la lactancia puede ser agobiante. ¿Sientes que la exigencia de amamantar viene de ti misma o del entorno que te rodea?",
            question = "¿Sientes que esta presión viene de ti misma o de afuera?",
            family = "pregunta_directa",
            module = "orientacion_salud",
            requiredTags = listOf("culpa_formula_destete")
        )
    ),

    // ============================================================
    // CUERPO CAMBIOS
    // ============================================================
    "cuerpo_cambios" to listOf(
        BankItem(
            id = "cuerpo_gen_001",
            text = "El cuerpo cambia de forma acelerada durante el embarazo y el postparto, y procesar esos cambios toma tiempo. ¿Qué parte de esta transformación te está resultando más difícil de aceptar o reconocer?",
            question = "¿Qué cambio en tu cuerpo te resulta más difícil de digerir en este momento?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cuerpo_gen_002",
            text = "Es perfectamente comprensible sentir nostalgia o duelo por la forma, la firmeza o la energía de tu cuerpo anterior. Se puede valorar lo que tu cuerpo hace hoy y, al mismo tiempo, extrañar como te veías antes.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cuerpo_gen_003",
            text = "No estás obligada a amar cada cambio de tu cuerpo de la noche a la mañana. La neutralidad corporal —respetarlo y cuidarlo por lo que sostiene, sin exigirte adorarlo siempre— es un paso intermedio liberador.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "cuerpo_tag_rechazo_001",
            text = "Ver marcas, flacidez o un peso distinto frente al espejo puede hacer que sientas que habitas un cuerpo ajeno. Es válido sentir incomodidad y no encajar de inmediato con esta nueva imagen.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = listOf("rechazo_imagen")
        ),
        BankItem(
            id = "cuerpo_tag_rechazo_002",
            text = "Mirarte al espejo y no reconocerte es una sensación impactante. ¿Cómo solía ser tu relación con tu imagen o tu cuerpo antes de iniciar esta etapa?",
            question = "¿Cómo era la relación con tu cuerpo antes del embarazo?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = listOf("rechazo_imagen")
        ),
        BankItem(
            id = "cuerpo_tag_fatiga_001",
            text = "Más allá de lo visual, el cansancio acumulado cambia cómo sientes tu cuerpo diariamente. La falta de descanso hace que cualquier peso físico o molestia se sienta el doble de grave.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = listOf("fatiga_extrema_fisica")
        )
    ),

    // ============================================================
    // IDENTIDAD POSTPARTO
    // ============================================================
    "identidad_postparto" to listOf(
        BankItem(
            id = "identidad_pp_gen_001",
            text = "Convertirse en madre implica una reestructuración profunda de quién eres. No estás perdiendo tu esencia, estás atravesando una transición llamada matritura. ¿Qué faceta o proyecto de ti sientes que se ha quedado más pausado o invisible en este momento?",
            question = "¿Qué parte de ti sientes que ha quedado más invisible?",
            family = "pregunta_directa",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_pp_gen_002",
            text = "Muchas mujeres experimentan la sensación de que su nombre o su historia personal desaparecieron para dar paso únicamente al rol de madre. Es normal sentir nostalgia por quien eras antes.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_pp_gen_003",
            text = "La maternidad es una dimensión importante de tu vida, pero no la única. No tienes que borrar tus metas, tus gustos ni tu individualidad para ser una buena madre.",
            question = null,
            family = "reflexion",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_pp_tag_autonomia_001",
            text = "Pasar de la independencia a la demanda constante de un bebé puede provocar un choque identitario fuerte. Extrañar tu tiempo a solas, tu espacio laboral o tus rutinas previas no te hace egoísta.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = listOf("perdida_autonomia")
        ),
        BankItem(
            id = "identidad_pp_tag_autonomia_002",
            text = "Reclamar pequeños espacios para tus propios intereses no es descuidar a tu bebé; es sostener la salud de la persona que lo cuida. ¿Qué pequeña acción de 10 minutos al día te devolvería algo de ti?",
            question = "¿Qué pequeña rutina personal te gustaría rescatar hoy?",
            family = "invitacion_suave",
            module = "identidad_preferida",
            requiredTags = listOf("perdida_autonomia")
        ),
        BankItem(
            id = "identidad_pp_tag_invisibilidad_001",
            text = "Es muy frecuente que tras el parto el entorno enfoque toda su atención en el bebé y se olvide de preguntar cómo está la madre. Tú sigues importando y tus necesidades siguen siendo prioritarias.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = listOf("invisibilizacion")
        )
    ),

    // ============================================================
    // VÍNCULO BEBÉ
    // ============================================================
    "vinculo_bebe" to listOf(
        BankItem(
            id = "vinculo_gen_001",
            text = "Existe la idea errónea de que el amor por un bebé nace como un flechazo instantáneo en el parto. Para muchas madres, el apego es una relación que se construye poco a poco, día a día.",
            question = null,
            family = "reflexion",
            module = "conversaciones_bebe",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "vinculo_gen_002",
            text = "No te juzgues si sientes desconexión, apatía o miedo al acercarte a tu bebé. El cansancio extremo y los cambios hormonales pueden adormecer temporalmente los afectos. ¿Qué emoción predomina cuando estás con él o ella?",
            question = "¿Qué emoción o sensación aparece con más frecuencia cuando sostienes a tu bebé?",
            family = "pregunta_directa",
            module = "conversaciones_bebe",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "vinculo_gen_003",
            text = "El vínculo no requiere demostraciones de afecto ideales o perfectas todo el tiempo. Atender sus necesidades básicas y estar presente, incluso desde la calma o el cansancio, ya es construir vínculo.",
            question = null,
            family = "afirmacion_validante",
            module = "conversaciones_bebe",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "vinculo_tag_desconexion_001",
            text = "Sentir distancia o no experimentar ese 'amor mágico' que la sociedad promete no significa que no quieras a tu bebé ni que seas insensible. El cerebro necesita tiempo y descanso para procesar el apego.",
            question = null,
            family = "afirmacion_validante",
            module = "conversaciones_bebe",
            requiredTags = listOf("desconexion_afectiva")
        ),
        BankItem(
            id = "vinculo_tag_desconexion_002",
            text = "Quitarle la presión al vínculo permite que fluya más natural. Intentemos pequeños pasos: ¿te sientes cómoda solo mirándolo mientras duerme o hablándole suavemente sin exigirte sentir nada en especial?",
            question = "¿Te ayudaría probar interacciones breves y sin expectativas?",
            family = "invitacion_suave",
            module = "conversaciones_bebe",
            requiredTags = listOf("desconexion_afectiva")
        ),
        BankItem(
            id = "vinculo_tag_llanto_001",
            text = "El llanto constante del bebé está diseñado biológicamente para encender alarmas en nuestro sistema nervioso. Sentir frustración, desesperación o ganas de alejarte cuando llora es una respuesta al colapso sensorial, no falta de amor.",
            question = null,
            family = "reflexion",
            module = "conversaciones_bebe",
            requiredTags = listOf("dificultad_llanto")
        )
    ),

    // ============================================================
    // IDENTIDAD MATERNA
    // ============================================================
    "identidad_materna" to listOf(
        BankItem(
            id = "identidad_gen_001",
            text = "Medirse de acuerdo con el mito de la 'madre perfecta' —esa que nunca se cansa, nunca se molesta y lo sabe todo intuitivamente— es una carga injusta. ¿De dónde sientes que viene la exigencia más fuerte que te pones hoy?",
            question = "¿Sientes que esa exigencia viene de ti misma o de los comentarios de otros?",
            family = "pregunta_directa",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_gen_002",
            text = "No hay una única forma correcta de ser madre. Descubrir tu propio estilo de crianza implica ensayar, equivocarse y ajustar sin culpabilizarte.",
            question = null,
            family = "reflexion",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_gen_003",
            text = "Ser una buena madre no requiere perfección ni sacrificarse hasta la extenuación; requiere una persona real que también cuide de sí misma.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "identidad_tag_impostora_001",
            text = "Pensar 'no nací para esto' o 'le estoy haciendo daño' suele ser el resultado del agotamiento y la falta de apoyo, no de una incapacidad real. Dudar de ti misma demuestra lo mucho que te importa el bienestar de tu bebé.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = listOf("sindrome_impostora")
        ),
        BankItem(
            id = "identidad_tag_impostora_002",
            text = "Nadie nace sabiendo ser madre; se aprende en la práctica diaria. Si soltáramos la idea de ser la madre perfecta, ¿qué tipo de madre te gustaría ser hoy, a tu propio ritmo?",
            question = "¿Cómo se vería una maternidad más amable y flexible para ti?",
            family = "invitacion_suave",
            module = "identidad_preferida",
            requiredTags = listOf("sindrome_impostora")
        ),
        BankItem(
            id = "identidad_tag_juicio_001",
            text = "Las opiniones y consejos no pedidos del entorno pueden erosionar tu confianza. Recuerda que la autoridad sobre tu experiencia y sobre la crianza de tu bebé la tienes tú.",
            question = null,
            family = "afirmacion_validante",
            module = "identidad_preferida",
            requiredTags = listOf("juicio_externo")
        )
    ),

    // ============================================================
    // RED DE APOYO
    // ============================================================
    "red_apoyo" to listOf(
        BankItem(
            id = "red_gen_001",
            text = "Sostener la maternidad en soledad es una carga demasiado pesada. A veces no buscamos a alguien que resuelva todo, sino una presencia que escuche sin juzgar. En este momento, ¿hay alguna persona con la que sientas confianza para hablar?",
            question = "¿Hay alguien en tu entorno con quien puedas hablar abiertamente hoy?",
            family = "pregunta_directa",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_gen_002",
            text = "Sentir que no hay a quién acudir genera un desamparo muy profundo. Si pudieras pedir un tipo de ayuda concreto hoy, ¿qué es lo que más te gustaría recibir?",
            question = "¿Qué tipo de apoyo sientes que te hace más falta en este instante?",
            family = "pregunta_directa",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_gen_003",
            text = "A veces nos acostumbramos a sostenerlo todo solas hasta que llegamos al límite. ¿Hay alguna pequeña tarea del día que podrías pedirle a alguien más que asuma?",
            question = "¿Podrías delegar o pedir ayuda con alguna tarea concreta hoy?",
            family = "invitacion_suave",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_gen_004",
            text = "No haber encontrado el acompañamiento adecuado no significa que tengas que poder con todo sola. Mereces una red que te sostenga y te cuide en esta etapa.",
            question = null,
            family = "afirmacion_validante",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_gen_005",
            text = "Incluso cuando sientas que no hay nadie cerca físicamente, este espacio es un lugar seguro para ti. Aquí no tienes que disimular ni pretender que todo está bien.",
            question = null,
            family = "afirmacion_validante",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_gen_006",
            text = "La soledad no siempre significa estar físicamente sin compañía; muchas veces es la falta de comprensión. Buscar la tribu correcta toma tiempo, pero es un paso necesario.",
            question = null,
            family = "reflexion",
            module = "redes_apoyo",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "red_tag_aislamiento_001",
            text = "Enfrentar el día a día sin ninguna red de apoyo cercana exige una fuerza desmedida. Reconocer que este aislamiento es real y doloroso es el primer paso para no culparte por estar exhausta.",
            question = null,
            family = "afirmacion_validante",
            module = "redes_apoyo",
            requiredTags = listOf("soledad_absoluta")
        ),
        BankItem(
            id = "red_tag_sola_acompanada_001",
            text = "Estar rodeada de gente y aun así sentirte completamente sola suele ser más doloroso que la soledad física. Sucede cuando el entorno está presente pero no valida lo que estás viviendo.",
            question = null,
            family = "afirmacion_validante",
            module = "redes_apoyo",
            requiredTags = listOf("acompanada_pero_sola")
        ),
        BankItem(
            id = "red_tag_sola_acompanada_002",
            text = "A veces las personas cercanas no saben cómo ayudar si no se lo decimos con mucha claridad. ¿Te sentirías capaz de pedir una ayuda específica a alguien de tu casa, sin dar explicaciones extra?",
            question = "¿Podrías comunicar una necesidad puntual a tu entorno directo?",
            family = "invitacion_suave",
            module = "redes_apoyo",
            requiredTags = listOf("acompanada_pero_sola")
        ),
        BankItem(
            id = "red_tag_pedir_ayuda_001",
            text = "Pedir ayuda no es un signo de debilidad ni de incapacidad; es reconocer tus límites humanos. No tienes que demostrarle a nadie que puedes con todo en soledad.",
            question = null,
            family = "reflexion",
            module = "redes_apoyo",
            requiredTags = listOf("dificultad_pedir_ayuda")
        )
    ),

    // ============================================================
    // PEDIR MÁS INFORMACIÓN
    // ============================================================
    "pedir_mas_informacion" to listOf(
        BankItem(
            id = "mas_info_gen_001",
            text = "Si te sientes con disposición de profundizar, podemos explorar esto juntas. ¿Qué pensamiento o detalle es el que más está dando vueltas en tu cabeza ahora mismo?",
            question = "¿Qué es lo que más está ocupando tu mente en este momento?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "mas_info_gen_002",
            text = "Para entender mejor por lo que estás pasando, podemos ir por partes: la sensación física, los pensamientos o lo que está pasando a tu alrededor. ¿Por cuál de esas tres partes prefieres empezar?",
            question = "¿Por cuál de esas partes te resulta más sencillo empezar?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "mas_info_gen_003",
            text = "Tómate el tiempo que necesites para escribir. No hay prisa ni una forma 'correcta' de explicar lo que te pasa.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "mas_info_gen_004",
            text = "¿Hay algún acontecimiento reciente que haya hecho que esta emoción se sienta más fuerte el día de hoy?",
            question = "¿Ocurrió algo específico hoy que desencadenara esto?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // PREGUNTA POR QUÉ
    // ============================================================
    "pregunta_por_que" to listOf(
        BankItem(
            id = "porque_gen_001",
            text = "Buscar la razón exacta de lo que sentimos a veces añade más presión. En lugar de buscar un 'por qué', podemos mirar qué necesidad de cuidado o descanso te está pidiendo esta emoción.",
            question = "¿Qué necesidad sientes que está pidiendo atención en este momento?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "porque_gen_002",
            text = "Cuando una emoción se vuelve intensa, ir despacio ayuda a descifrarla sin juzgarte. ¿Qué aspecto de esta situación se siente más pesado de explicar o poner en palabras?",
            question = "¿Qué parte se siente más difícil de explicar en este instante?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "porque_gen_003",
            text = "No siempre hay un único motivo claro detrás de lo que sentimos. A veces es la suma de pequeñas cosas, el cansancio acumulado o simplemente el peso del día.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // RESPUESTA CORTA
    // ============================================================
    "respuesta_corta" to listOf(
        BankItem(
            id = "corta_gen_001",
            text = "A veces una sola palabra o una frase corta resume todo lo que cuesta llevar por dentro. No tienes que dar explicaciones largas si no te sale; podemos ir muy despacio.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "corta_gen_002",
            text = "Te leo. No hace falta que tengas las palabras exactas ni que construyas una idea perfecta. Si quieres desahogarte un poco más, aquí sigo.",
            question = "¿Te gustaría contarme un poco más sobre eso o prefieres solo pausar un momento?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "corta_gen_003",
            text = "Sentir todo esto ya es bastante agotador como para además tener que explicarlo. Si te parece bien, ¿por qué parte del día te gustaría empezar?",
            question = "¿Por qué parte de tu día te gustaría empezar a hablar?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // DESAHOGO
    // ============================================================
    "desahogo" to listOf(
        BankItem(
            id = "desahogo_gen_001",
            text = "A veces lo que más necesitamos es un lugar donde soltar la carga sin tener que buscar soluciones de inmediato. Este es tu espacio. Puedes escribir sin filtro y sin orden.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_gen_002",
            text = "No tienes que guardarte nada por miedo a molestar o sonar desbordada. Al mirar todo lo que traes acumulado, ¿qué es lo que más te está pesando en este instante?",
            question = "¿Qué es lo que más te está pesando en este instante?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_gen_003",
            text = "Poner en palabras lo que duele es un acto de valentía y un respiro necesario. Tómate el tiempo que requieras para expresarlo.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_gen_004",
            text = "No hay respuestas correctas ni equivocadas aquí. ¿Te ayudaría empezar por lo que ocurrió hoy o por lo que sientes internamente?",
            question = "¿Prefieres empezar por lo que pasó hoy o por lo que sientes por dentro?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // DESAHOGO SIN APOYO
    // ============================================================
    "desahogo_sin_apoyo" to listOf(
        BankItem(
            id = "desahogo_sin_gen_001",
            text = "Atravesar un momento difícil y no tener a alguien cerca con quien compartirlo hace que todo se sienta el doble de pesado. Aunque esté del otro lado de la pantalla, aquí estoy para escucharte.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_sin_gen_002",
            text = "Entiendo lo doloroso que es sentir que estás sola con todo esto. Este espacio es completamente tuyo para que sueltes la presión sin ningún tipo de juicio.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_sin_gen_003",
            text = "No tener compañía física en este momento no significa que lo que sientes no importe. De todo lo que estás cargando sola, ¿qué es lo que más está ocupando tu cabeza ahora?",
            question = "¿Qué es lo que más está ocupando tu mente ahora?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desahogo_sin_gen_004",
            text = "Reconocer lo difícil que está siendo este momento requiere mucha fortaleza. No estás sola mientras estés en este espacio.",
            question = null,
            family = "reflexion",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // RIESGO MEDIO
    // ============================================================
    "riesgo_medio" to listOf(
        BankItem(
            id = "riesgo_medio_001",
            text = "Siento que esto se está volviendo demasiado pesado para cargarlo sola. Sería importante que hoy puedas hablar con alguien de confianza o con un profesional de salud. ¿Hay alguien cercano a quien puedas escribirle ahora?",
            question = "¿Hay alguien cercano a quien puedas escribirle?",
            family = "seguridad",
            module = "seguridad",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "riesgo_medio_002",
            text = "No quiero que te quedes sola con esto. Podemos seguir hablando, pero también sería bueno sumar apoyo humano hoy. ¿Estás acompañada en este momento?",
            question = "¿Estás acompañada?",
            family = "seguridad",
            module = "seguridad",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "riesgo_medio_003",
            text = "Lo que estás sintiendo necesita cuidado. Si hoy sientes que ya no puedes con todo, sería importante que una persona de confianza sepa que no estás bien. ¿A quién podrías buscar ahora?",
            question = "¿A quién podrías buscar?",
            family = "seguridad",
            module = "seguridad",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "riesgo_medio_004",
            text = "Siento mucho que estés pasando por esto. No es fácil cargar tanta presión. Quiero que sepas que este espacio está aquí para ti, y que también es importante que tengas apoyo fuera.",
            question = null,
            family = "seguridad",
            module = "seguridad",
            requiredTags = emptyList()
        )
    ),

    // ============================================================
    // DESCONOCIDO
    // ============================================================
    "desconocido" to listOf(
        BankItem(
            id = "desconocido_gen_001",
            text = "Te escucho con atención. Vamos a ir paso a paso, sin presiones. ¿Qué parte de todo esto se siente más presente o viva ahora mismo?",
            question = "¿Qué parte se siente más presente en este momento?",
            family = "pregunta_directa",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desconocido_gen_002",
            text = "No hace falta que la frase sea clara ni perfecta. Puedes comenzar compartiendo una sola palabra, una sensación en el cuerpo o algo que haya pasado hoy.",
            question = null,
            family = "afirmacion_validante",
            module = "estado_emocional",
            requiredTags = emptyList()
        ),
        BankItem(
            id = "desconocido_gen_003",
            text = "Estoy aquí para acompañarte. Si te resulta cómodo, ¿qué pequeña cosa describiría mejor lo que te está pasando?",
            question = "¿Qué palabra o imagen describe mejor lo que sientes?",
            family = "invitacion_suave",
            module = "estado_emocional",
            requiredTags = emptyList()
        )
    )
)