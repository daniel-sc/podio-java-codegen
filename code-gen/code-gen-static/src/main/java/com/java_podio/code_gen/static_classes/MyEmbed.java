package com.java_podio.code_gen.static_classes;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.podio.embed.Embed;

/**
 * Fixing a parsing error with created_on and jackson.<br>
 * TODO: merge into podio-java / a newer jackson version might solve this?
 */
public class MyEmbed extends Embed {
    
    @Override
    @JsonProperty("created_on")
    @JsonDeserialize(using = MyEmbed.MyDateDeserlializer.class)
    public void setCreatedOn(DateTime createdOn) {
        super.setCreatedOn(createdOn);
    }

    public static class MyDateDeserlializer extends JsonDeserializer<DateTime> {
        
        public MyDateDeserlializer() {
            super();
        }
    
        @Override
        public DateTime deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException,
        	JsonProcessingException {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            return formatter.parseDateTime(arg0.getText());
        }
    
    }
}