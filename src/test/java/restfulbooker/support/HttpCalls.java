package restfulbooker.support;

import io.restassured.internal.http.HttpResponseException;
import io.restassured.response.Response;

import java.util.function.Supplier;

/**
 * REST Assured's underlying HTTPBuilder throws {@link HttpResponseException} from inside
 * get()/put()/patch()/delete() for any non-2xx response (post() does not do this). That means a
 * test asserting on an expected 401/403/404/400 never reaches its own assertion - the call
 * itself throws first. This wraps such a call so the status code is returned normally instead,
 * regardless of whether it is 2xx or not.
 */
public final class HttpCalls {

    private HttpCalls() {
    }

    public static int statusCode(Supplier<Response> call) {
        try {
            return call.get().statusCode();
        } catch (Exception e) {
            // HttpResponseException extends the checked java.io.IOException, but Groovy (which
            // implements the get()/put()/patch()/delete() calls) does not declare it - so Java
            // cannot catch it by its own type here without first catching the common Exception.
            if (e instanceof HttpResponseException httpResponseException) {
                System.out.println("Request did not return 2xx; observed status code: " + httpResponseException.getStatusCode());
                return httpResponseException.getStatusCode();
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        }
    }
}
