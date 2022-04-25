package com.csd.common.request;

import java.time.OffsetDateTime;
import java.util.Objects;

public class StartSessionRequestBody implements IRequest {
    private OffsetDateTime timestamp;

    public StartSessionRequestBody(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public StartSessionRequestBody() {
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StartSessionRequestBody{" +
                "timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartSessionRequestBody that = (StartSessionRequestBody) o;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }

    @Override
    public Type type() {
        return Type.SESSION;
    }
}
