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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@General(title = "Document Flattener 2", author = "Your Company Name",
        purpose = "Flatten an input document with nested fields",
        docLink = "http://yourdocslinkhere.com")
@Inputs(min = 1, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 2)
@Category(snap = SnapCategory.TRANSFORM)
public class DocumentFlattener2 extends SimpleSnap {
    private List<String> passThroughFields;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
//        propertyBuilder.describe("pass", "Pass-through Fields",
//                        "Fields that should be passed through unmodified")
//                .schemaAware(SnapProperty.DecoratorType.ACCEPTS_SCHEMA)
//                .type(SnapProperty.Type.STRING_SET)
//                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) {
//        passThroughFields = new ArrayList<>(propertyValues.getAsSet("pass"));
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

//            if (passThroughFields.contains(key)) {
//                flattened.put(key, value);
//            } else
            if (value instanceof Map) {
                flattenMap((Map<String, Object>) value, prefix + key + ".", flattened);
            } else if (value instanceof List) {
                flattenList((List<Object>) value, prefix + key + ".", flattened);
            } else {
                flattened.put(prefix + key, value);
            }
        }
    }

    private void flattenList(List<Object> original, String prefix, Map<String, Object> flattened) {
        for (int i = 0; i < original.size(); i++) {
            Object value = original.get(i);

            if (value instanceof Map) {
                flattenMap((Map<String, Object>) value, prefix + i + ".", flattened);
            } else if (value instanceof List) {
                flattenList((List<Object>) value, prefix + i + ".", flattened);
            } else {
                flattened.put(prefix + i, value);
            }
        }
    }
}
