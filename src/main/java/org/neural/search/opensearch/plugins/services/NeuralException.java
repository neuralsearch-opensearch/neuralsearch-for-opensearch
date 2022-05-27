package org.neural.search.opensearch.plugins.services;

import java.io.IOException;

public class NeuralException extends IOException{

    public NeuralException(String message, Throwable cause) {
        super(message, cause);
    }
}
