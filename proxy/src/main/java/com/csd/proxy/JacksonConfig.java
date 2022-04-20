package com.csd.proxy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.stringToBytes;

@JsonComponent
public class JacksonConfig {

    public static class BytesJsonSerializer extends JsonSerializer<byte[]> {

        @Override
        public void serialize(byte[] value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(bytesToString(value));
        }
    }

    public static class BytesJsonDeserializer extends JsonDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            return stringToBytes(node.asText());
        }
    }
}
