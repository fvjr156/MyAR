package com.fvjapps.myar;

public class Model {
    private String modelPath;
    private String modelName;
    private Float scale;

    public Model(String modelPath, String modelName, Float scale) {
        this.modelPath = modelPath;
        this.modelName = modelName;
        this.scale = scale;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getModelName() {
        return modelName;
    }

    public Float getScale() {
        return scale;
    }
}
