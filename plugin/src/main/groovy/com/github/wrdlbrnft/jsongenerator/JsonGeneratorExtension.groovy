package com.github.wrdlbrnft.jsongenerator

import org.gradle.api.Named

/**
 * Created by Xaver on 17/09/16.
 */
class JsonGeneratorExtension implements Named {

    private final String name;

    File template;
    File output;
    Map<String, Object> variables = new HashMap<>();

    public JsonGeneratorExtension(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    void template(File template) {
        this.template = template
    }

    void output(File output) {
        this.output = output
    }

    void variables(Map<String, Object> variables) {
        this.variables = variables
    }
}
