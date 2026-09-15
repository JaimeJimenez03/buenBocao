package com.buenbocao.api.common.enums;

/**
 * Unidades de medida para ingredientes.
 */
public enum MeasurementUnit {

    // Peso
    GRAMS("g"),
    KILOGRAMS("kg"),
    OUNCES("oz"),
    POUNDS("lb"),

    // Volumen
    MILLILITERS("ml"),
    LITERS("L"),
    TEASPOON("cucharadita"),
    TABLESPOON("cucharada"),
    CUP("taza"),

    // Unidades
    PIECE("ud"),
    PINCH("pizca"),
    TO_TASTE("al gusto");

    private final String abbreviation;

    MeasurementUnit(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}