package io.gihub.jlmc.poc.commons.settings.http;

import java.util.Map;
import java.util.Objects;

public sealed interface Body
        permits Body.Form, Body.StringBody {

    record Form(Map<String, String> data) implements Body {
        public Form {
            Objects.requireNonNull(data, "data must not be null");
        }
    }

    record StringBody(String value, String contentType) implements Body {
        public StringBody {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(contentType, "contentType must not be null");
        }
    }
}
