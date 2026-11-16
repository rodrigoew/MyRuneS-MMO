package com.client.definitions;
/** Twelve original Jagex item definitions from cache1175/build210 (2022-12-13).
 * Only string terminators and unused modern metadata were converted for this client.
 * Models, colours, textures, offsets and worn graphics retain original numeric data.
 * Provenance and raw originals accompany the package validation assets. */
final class AdventurerGraceful2022 {
    private static final java.util.Map<Integer,String> ENCODED=java.util.Map.ofEntries(
        java.util.Map.entry(27444, "XgWVAbYeF7YLABm2EQBathhbthkHAAII//gEAzYGAGwFAJYkV2VhcgoQDAAAACMCR3JhY2VmdWwgaG9vZAqUazUA"),
        java.util.Map.entry(27446, "XgWVAbYeF7YLABm2EQBathhbthkHAAII//gEAzYGAGwFAJYkV2VhcgoMAAAAIxACR3JhY2VmdWwgaG9vZAoA"),
        java.util.Map.entry(27447, "XgWVAbYdF7YNABm2EwAECRAHAAIIAAIFAjQGAzskV2VhcgoMAAAAKBACR3JhY2VmdWwgY2FwZQqUazgA"),
        java.util.Map.entry(27449, "XgWVAbYdF7YNABm2EwAECRAHAAIIAAIFAjQGAzskV2VhcgoMAAAAKBACR3JhY2VmdWwgY2FwZQoA"),
        java.util.Map.entry(27450, "XgWVAbYbF7YPABm2FQAEBOYFAfQI//oHAAMkV2VhcgoMAAAANxACR3JhY2VmdWwgdG9wCpRrOwA="),
        java.util.Map.entry(27452, "XgWVAbYbF7YPABm2FQAEBOYFAfQI//oHAAMkV2VhcgoMAAAANxACR3JhY2VmdWwgdG9wCgA="),
        java.util.Map.entry(27453, "XgWVAbYaF7YOABm2FAAI//0EBhQFAjwkV2VhcgoMAAAAPBACR3JhY2VmdWwgbGVncwqUaz4A"),
        java.util.Map.entry(27455, "XgWVAbYaF7YOABm2FAAI//0EBhQFAjwkV2VhcgoMAAAAPBACR3JhY2VmdWwgbGVncwoA"),
        java.util.Map.entry(27456, "XgWVAYGWFxwnABmBggAEAlAHAAMIAAMFAnwGB98kV2VhcgoMAAAAHhAoAiiYDZwrLxCrAkdyYWNlZnVsIGdsb3ZlcwqUa0EA"),
        java.util.Map.entry(27458, "XgWVAYGWFxwnABmBggAEAlAHAAMIAAMFAnwGB98kV2VhcgoMAAAAHhAoAiiYDZwrLxCrAkdyYWNlZnVsIGdsb3ZlcwoA"),
        java.util.Map.entry(27459, "XgWVAbYfF7YQABm2FgAHAAEI//sEAvUGAKAFAJgkV2VhcgoMAAAAKBACR3JhY2VmdWwgYm9vdHMKlGtEAA=="),
        java.util.Map.entry(27461, "XgWVAbYfF7YQABm2FgAHAAEI//sEAvUGAKAFAJgkV2VhcgoMAAAAKBACR3JhY2VmdWwgYm9vdHMKAA==")
    );
    private static final java.util.Map<Integer,byte[]> DEFINITIONS=new java.util.HashMap<>();
    static{ENCODED.forEach((id,data)->DEFINITIONS.put(id,java.util.Base64.getDecoder().decode(data)));}
    private AdventurerGraceful2022(){}
    static byte[] bytes(int id){return DEFINITIONS.get(id);}
}
