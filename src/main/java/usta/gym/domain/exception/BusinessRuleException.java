package usta.gym.domain.exception;

// Se lanza cuando se viola una regla de negocio. La vista la muestra como alerta roja.
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
