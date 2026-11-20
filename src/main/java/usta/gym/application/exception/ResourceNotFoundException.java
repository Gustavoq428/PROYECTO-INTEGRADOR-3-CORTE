package usta.gym.application.exception;

// Se lanza cuando se busca algo por id y no existe (la vista responde con la página 404)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
