package com.fluxaj.service;

import java.util.regex.Pattern;

public class SubscriptionRegexPatterns {

    // --- MONTOS ---
    // Detecta: $15.99 | USD 9.99 | COP 50.000 | 12,99 EUR
    public static final Pattern MONTO = Pattern.compile(
        "(?:USD|COP|EUR|\\$|€)?\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{1,2})?)" +
        "\\s*(?:USD|COP|EUR)?",
        Pattern.CASE_INSENSITIVE
    );

    // --- FECHAS ---
    // Detecta: 15/03/2025 | March 15, 2025 | 2025-03-15 | 15 de marzo de 2025
    public static final Pattern FECHA = Pattern.compile(
        "(?:" +
        "\\b(\\d{1,2})[/\\-](\\d{1,2})[/\\-](\\d{2,4})\\b" +          // 15/03/2025
        "|\\b(\\d{4})[/\\-](\\d{2})[/\\-](\\d{2})\\b" +                // 2025-03-15
        "|(january|february|march|april|may|june|july|august|" +
        "september|october|november|december)\\s+(\\d{1,2}),?\\s+(\\d{4})" + // March 15, 2025
        "|(\\d{1,2})\\s+de\\s+(enero|febrero|marzo|abril|mayo|junio|" +
        "julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+de\\s+(\\d{4})" + // 15 de marzo de 2025
        ")",
        Pattern.CASE_INSENSITIVE
    );

    // --- PROVEEDORES CONOCIDOS ---
    // Detecta el nombre del servicio en el asunto o remitente
    public static final Pattern PROVEEDOR = Pattern.compile(
        "\\b(netflix|spotify|youtube\\s*premium|amazon\\s*prime|" +
        "disney\\+?|hbo\\s*max|apple\\s*tv|apple\\s*music|" +
        "microsoft\\s*365|office\\s*365|adobe|canva|" +
        "dropbox|google\\s*one|icloud|github|" +
        "zoom|slack|notion|figma|chatgpt|openai|" +
        "duolingo|coursera|udemy|linkedin\\s*premium|" +
        "playstation\\s*plus|xbox\\s*game\\s*pass|steam|" +
        "gym|gimnasio|fitness)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // --- PALABRAS CLAVE que indican que es un recibo ---
    public static final Pattern ES_RECIBO = Pattern.compile(
        "\\b(invoice|receipt|payment|subscription|billing|charge|" +
        "factura|recibo|pago|suscripción|cobro|renovación|renewal|" +
        "your\\s+receipt|your\\s+payment|gracias\\s+por\\s+tu\\s+pago|" +
        "confirmación\\s+de\\s+pago|order\\s+confirmation)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // --- CATEGORÍAS automáticas ---
    public static final Pattern STREAMING = Pattern.compile(
        "\\b(netflix|spotify|youtube|disney|hbo|apple\\s*tv|apple\\s*music|" +
        "deezer|tidal|amazon\\s*prime\\s*video)\\b", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern TRABAJO = Pattern.compile(
        "\\b(microsoft|office|adobe|slack|zoom|notion|figma|github|" +
        "jira|confluence|dropbox|google\\s*workspace|canva)\\b", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern SALUD = Pattern.compile(
        "\\b(gym|gimnasio|fitness|headspace|calm|meditacion|noom|" +
        "whoop|strava|peloton)\\b", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern GAMING = Pattern.compile(
        "\\b(playstation|xbox|steam|nintendo|ea\\s*play|ubisoft|" +
        "game\\s*pass|twitch|discord\\s*nitro)\\b", Pattern.CASE_INSENSITIVE
    );
}