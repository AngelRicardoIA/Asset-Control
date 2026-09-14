package com.assetcontrol.documents.application;

import java.util.Map;

public interface DocxTemplateRenderer {

    byte[] render(Map<String, String> values);
}
