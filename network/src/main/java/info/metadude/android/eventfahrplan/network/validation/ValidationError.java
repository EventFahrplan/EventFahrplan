package info.metadude.android.eventfahrplan.network.validation;

class ValidationError extends Error {

    private static final long serialVersionUID = 1L;

    ValidationError(String message) {
        super(message);
    }

}
