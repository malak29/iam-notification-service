package com.iam.notification.config;

public final class Messages {

    private Messages() {}

    // Success messages
    public static final String EMAIL_SENT_SUCCESS = "Email sent successfully";
    public static final String SMS_SENT_SUCCESS = "SMS sent successfully";
    public static final String TEMPLATED_EMAIL_SENT_SUCCESS = "Templated email sent successfully";
    public static final String BULK_NOTIFICATION_QUEUED = "Bulk notification queued successfully";
    public static final String REAL_TIME_SENT_SUCCESS = "Real-time notification sent successfully";
    public static final String PREFERENCE_UPDATED_SUCCESS = "Notification preference updated successfully";
    public static final String TEMPLATE_CREATED_SUCCESS = "Notification template created successfully";

    // Error messages
    public static final String TEMPLATE_NOT_FOUND = "Notification template not found: %s";
    public static final String USER_PREFERENCES_NOT_FOUND = "User notification preferences not found";
    public static final String INVALID_TEMPLATE_FORMAT = "Invalid template format";
    public static final String EMAIL_DISABLED = "Email notifications are disabled for this user";
    public static final String SMS_DISABLED = "SMS notifications are disabled for this user";
    public static final String PROVIDER_NOT_AVAILABLE = "Notification provider is not available";
    public static final String INVALID_PHONE_NUMBER = "Invalid phone number format";
    public static final String INVALID_EMAIL_ADDRESS = "Invalid email address format";
    public static final String TEMPLATE_PROCESSING_FAILED = "Failed to process notification template";
    public static final String QUEUE_PROCESSING_FAILED = "Failed to process notification queue";
    public static final String WEBSOCKET_CONNECTION_FAILED = "WebSocket connection failed";

    // Validation messages
    public static final String RECIPIENT_REQUIRED = "Recipient is required";
    public static final String MESSAGE_REQUIRED = "Message content is required";
    public static final String TEMPLATE_NAME_REQUIRED = "Template name is required";
    public static final String USER_ID_REQUIRED = "User ID is required";
    public static final String NOTIFICATION_TYPE_REQUIRED = "Notification type is required";

    // Provider messages
    public static final String AWS_SES_CONFIGURED = "AWS SES email provider configured";
    public static final String AWS_SNS_CONFIGURED = "AWS SNS SMS provider configured";
    public static final String SMTP_CONFIGURED = "SMTP email provider configured";
    public static final String TWILIO_CONFIGURED = "Twilio SMS provider configured";
    public static final String MOCK_SMS_CONFIGURED = "Mock SMS provider configured";
    public static final String FILE_TEMPLATE_STORAGE_CONFIGURED = "File template storage configured";
    public static final String AWS_S3_TEMPLATE_STORAGE_CONFIGURED = "AWS S3 template storage configured";

    // Stats messages
    public static final String STATS_RETRIEVED_SUCCESS = "Notification statistics retrieved successfully";
    public static final String DAILY_STATS_RETRIEVED = "Daily notification statistics retrieved";
    public static final String WEEKLY_STATS_RETRIEVED = "Weekly notification statistics retrieved";

    // Queue messages
    public static final String NOTIFICATION_QUEUED = "Notification queued for processing";
    public static final String NOTIFICATION_RETRY_QUEUED = "Notification queued for retry";
    public static final String MAX_RETRIES_EXCEEDED = "Maximum retry attempts exceeded";
    public static final String QUEUE_EMPTY = "Notification queue is empty";
}