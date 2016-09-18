package com.elizaveta.task;

import java.util.Date;

public class Command {
    private String externalId;
    private String message;
    private Date time;
    private String extraParams;
    private NotificationType type;

    public Command(String externalId, String message, Date time, String extraParams, NotificationType type) {
        this.externalId = externalId;
        this.message = message;
        this.time = time;
        this.extraParams = extraParams;
        this.type = type;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public NotificationType getType() {
        return type;
    }
}
