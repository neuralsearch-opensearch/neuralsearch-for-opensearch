package org.neural.search.opensearch.plugins;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import org.opensearch.client.Client;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Setting.Property;


public class NeuralSearchSettings {

    private static NeuralSearchSettings INSTANCE;
    Client client;

    public Client getClient()
    {
        return client;
    }

    public void init(Client client)
    {
        this.client = client;

    }
    /**
     * Settings name
     */
    public static final String NEURAL_SERVICES_URL = "neural.services.url";


    /**
     * Default setting values
     */
    public static final String NEURAL_SERVICES_URL_DEFAULT = "http://localhost:8080";

    /**
     * Settings Definition
     */


    public static final Setting<String> NEURAL_SERVICES_HOSTNAME_SETTING = Setting.simpleString(NEURAL_SERVICES_URL,NEURAL_SERVICES_URL_DEFAULT,
            new EmbeddingsServerUrlValidator(),
            Property.NodeScope,
            Setting.Property.Deprecated);
    

    static class EmbeddingsServerUrlValidator implements Setting.Validator<String> {

        @Override public void validate(String value) {
            try {
                //Check URL here
            } catch (IllegalArgumentException ex) {
                throw new InvalidParameterException(ex.getMessage());
            }
        }
    }

    private NeuralSearchSettings() {}

    public static synchronized NeuralSearchSettings state() {
        if (INSTANCE == null) {
            INSTANCE = new NeuralSearchSettings();
        }
        return INSTANCE;
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings =  Arrays.asList(NEURAL_SERVICES_HOSTNAME_SETTING);
        return settings;
    }

}