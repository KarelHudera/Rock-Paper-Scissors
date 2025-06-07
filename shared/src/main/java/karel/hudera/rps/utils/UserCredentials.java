package karel.hudera.rps.utils;

import java.io.Serializable;

/**
 * Represents authentication credentials for a user.
 */
public sealed interface UserCredentials extends Serializable {

    record BasicCredentials(String username, String password) implements UserCredentials {}
}