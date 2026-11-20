package usta.gym.domain.service;

import java.math.BigDecimal;
import java.util.Locale;

// Formato de dinero con punto de miles ($80.000), usado en mensajes y reportes
public final class MoneyFormat {

    private MoneyFormat() {
    }

    public static String format(BigDecimal value) {
        if (value == null) {
            return "$0";
        }
        return "$" + String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }
}
