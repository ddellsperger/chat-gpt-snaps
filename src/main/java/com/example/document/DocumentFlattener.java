package com.example.document;

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

import java.util.LinkedHashMap;
import java.util.Map;

@General(title = "Document Flattener", author = "Your Company Name",
        purpose = "Flatten an input document with nested fields",
        docLink = "http://yourdocslinkhere.com")
@Inputs(min = 1, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.TRANSFORM)
public class DocumentFlattener extends SimpleSnap {
    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
    }

    @Override
    public void configure(PropertyValues propertyValues) {
    }

    @Override
    protected void process(Document document, String inputViewName) {
        try {
            Map<String, Object> docAsMap = documentUtility.getAsMap(document, errorViews);
            Map<String, Object> flattenedDoc = new LinkedHashMap<>();
            flattenMap(docAsMap, "", flattenedDoc);

            outputViews.write(documentUtility.newDocument(flattenedDoc));
        } catch (Exception e) {
            throw new SnapDataException(e, "Error flattening input document");
        }
    }

    private void flattenMap(Map<String, Object> original, String prefix, Map<String, Object> flattened) {
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenMap((Map<String, Object>) value, prefix + key + ".", flattened);
            } else {
                flattened.put(prefix + key, value);
            }
        }
    }
}
