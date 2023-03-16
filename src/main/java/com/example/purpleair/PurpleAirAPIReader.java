package com.example.purpleair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.snaplogic.api.ConfigurationException;
//import com.snaplogic.api.DependencyManager;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

@General(title = "PurpleAir API Reader", author = "Your Company Name",
        purpose = "Retrieve air quality measurements from PurpleAir API",
        docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 0)
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class PurpleAirAPIReader extends SimpleSnap { // implements DependencyManager {
    private static final String API_KEY_PROP = "apiKey";

    private String apiKey;

    private ObjectMapper mapper;
    private PurpleAirAPI purpleAirAPI;

    public PurpleAirAPIReader() {
        this.mapper = new ObjectMapper();
        this.purpleAirAPI = new PurpleAirAPIImpl(mapper);
    }

    // @Inject
    // public PurpleAirAPIReader(ObjectMapper mapper, PurpleAirAPI purpleAirAPI) {
    //     this.mapper = mapper;
    //     this.purpleAirAPI = purpleAirAPI;
    // }

    // @Override
    // public Module getManagedModule() {
    //     return new AbstractModule() {
    //         @Override
    //         protected void configure() {
    //             bind(PurpleAirAPI.class).to(PurpleAirAPIImpl.class);
    //         }
    //     };
    // }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(API_KEY_PROP, "PurpleAir API Key",
                "API Key to access PurpleAir API")
                .expression()
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        apiKey = propertyValues.get(API_KEY_PROP);
    }

    @Override
    protected void process(Document document, String inputViewName) {
        try {
            Map<String, Object> airQualityData = purpleAirAPI.getAirQualityData(apiKey);
            if (airQualityData != null) {
                outputViews.write(documentUtility.newDocument(airQualityData));
            }
        } catch (IOException e) {
            throw new SnapDataException(e, "Error retrieving air quality data from PurpleAir API");
        }
    }

    public interface PurpleAirAPI {
        Map<String, Object> getAirQualityData(String apiKey) throws IOException;
    }

    public static class PurpleAirAPIImpl implements PurpleAirAPI {
        private static final String PURPLE_AIR_API_URL = "https://api.purpleair.com/v1/sensors?api_key=%s";
        private ObjectMapper mapper;
        private TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };

        @Inject
        public PurpleAirAPIImpl(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Map<String, Object> getAirQualityData(String apiKey) throws IOException {
            Map<String, Object> airQualityData;
            String url = String.format(PURPLE_AIR_API_URL, apiKey);

            try (InputStream inputStream = new URL(url).openStream()) {
                airQualityData = mapper.readValue(inputStream, mapTypeReference);
            } catch (IOException e) {
                throw new IOException("Error fetching data from PurpleAir API", e);
            }

            return airQualityData;
        }
    }
}
