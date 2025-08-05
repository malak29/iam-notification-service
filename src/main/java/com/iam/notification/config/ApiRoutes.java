package com.iam.notification.config;

public final class ApiRoutes {

    private ApiRoutes() {} // Prevent instantiation

    public static final String API_V1 = "/api/v1";

    // Main notification routes
    public static final String NOTIFICATIONS = API_V1 + "/notifications";
    public static final String PREFERENCES = API_V1 + "/notification-preferences";
    public static final String STATS = API_V1 + "/notification-stats";

    // Notification endpoints
    public static final String SEND_EMAIL = "/email";
    public static final String SEND_SMS = "/sms";
    public static final String SEND_TEMPLATED_EMAIL = "/templated-email";
    public static final String SEND_BULK = "/bulk";
    public static final String SEND_REAL_TIME = "/real-time";

    // Integration endpoints
    public static final String SEND_WELCOME_EMAIL = "/welcome-email";
    public static final String SEND_PASSWORD_RESET = "/password-reset";
    public static final String SEND_SECURITY_ALERT = "/security-alert";
    public static final String SEND_ROLE_CHANGE = "/role-change";

    // Preference endpoints
    public static final String USER_PREFERENCES = "/users/{userId}";
    public static final String UPDATE_PREFERENCE = "/users/{userId}";
    public static final String CREATE_DEFAULT_PREFERENCES = "/users/{userId}/defaults";

    // Stats endpoints
    public static final String DAILY_STATS = "/daily";
    public static final String WEEKLY_STATS = "/weekly";
    public static final String CUSTOM_STATS = "/custom";

    // Health endpoint
    public static final String HEALTH = "/health";
}