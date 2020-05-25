package ch.epfl.polychef;

/**
 * The interface of a CallHandler, can either success or fail its call.
 * @param <T> the result type
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface CallHandler<T> {

    /**
     * What happens if the callHandler successes.
     * @param data the required data
     */
    public void onSuccess(T data);

    /**
     * What happens if the callHandler fails.
     */
    public void onFailure();
}
