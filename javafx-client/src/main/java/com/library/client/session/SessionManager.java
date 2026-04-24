package com.library.client.session;

/**
 * Singleton session manager for JWT token storage and retrieval.
 * Thread-safe storage of the authentication token after login.
 * Token is attached to all subsequent API requests as "Authorization: Bearer {token}".
 */
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private String token;

    private SessionManager() {
    }

    /**
     * Gets the singleton instance.
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the current JWT token.
     *
     * @return JWT token or null if not logged in
     */
    public synchronized String getToken() {
        return token;
    }

    /**
     * Sets the JWT token (called after successful login).
     *
     * @param token JWT token from server
     */
    public synchronized void setToken(String token) {
        this.token = token;
    }

    /**
     * Clears the token (called on logout).
     */
    public synchronized void clear() {
        this.token = null;
    }

    /**
     * Checks if user is logged in.
     *
     * @return true if token is not null
     */
    public synchronized boolean isLoggedIn() {
        return token != null;
    }
}
