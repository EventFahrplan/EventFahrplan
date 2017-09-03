package nerd.tuxmobil.fahrplan.congress.validation;

public class ValidationError extends Error {

    private static final long serialVersionUID = 1L;

    public ValidationError(String message) {
        super(message);
    }

}
