package com.example.test;

import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.UUID;

public class CustomTransformableNode extends TransformableNode {
    private UUID identifier;

    public CustomTransformableNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }
}