package org.neural.search.opensearch.plugins.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class NeuralServices {

    public static float[] getEmbedding(String apiUrl, String text, String modelName) throws IOException
    {
        if(!apiUrl.endsWith("/"))
        apiUrl+="/";
        apiUrl+="embedding";

        EmbeddingParam param = new EmbeddingParam(text, modelName);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String data = ow.writeValueAsString(param);
        String result = callRemoteService(apiUrl, data);
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode tree = mapper.readTree(result);

        float[] arr = mapper.convertValue(tree.get("embedding"), float[].class);
        return arr;

    }
    

    public static String callRemoteService(String apiUrl, String data) throws IOException
    {
        URL url = new URL(apiUrl);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Content-Type", "application/json");

        //String data = "{\n  \"Id\": 78912,\n  \"Customer\": \"Jason Sweet\",\n  \"Quantity\": 1,\n  \"Price\": 18.00\n}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        BufferedReader in = new BufferedReader(
        new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        http.disconnect();
        return content.toString();
    }

    public static NeuralAnswer answer(String apiUrl, String question, String context, String modelName) throws IOException
    {
        if(!apiUrl.endsWith("/"))
        apiUrl+="/";
        apiUrl+="qa";

        QAParam param = new QAParam(context, question, modelName);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String data = ow.writeValueAsString(param);

        String result = callRemoteService(apiUrl, data);
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode tree = mapper.readTree(result);

        String answer = mapper.convertValue(tree.get("answer"), String.class);
        String answerHighlight = mapper.convertValue(tree.get("answer_highlight"), String.class);
        float score = mapper.convertValue(tree.get("score"), Float.class);

        NeuralAnswer neuralAnswer = new NeuralAnswer(answer, answerHighlight, score);
        return neuralAnswer;
    }

    public static List<SentenceScore> scoreSentences(String apiUrl, String text, String context, String modelName) throws IOException
    {
        if(!apiUrl.endsWith("/"))
        apiUrl+="/";
        apiUrl+="sentences";

        SentencesParam param = new SentencesParam(text, context, modelName);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String data = ow.writeValueAsString(param);

        String result = callRemoteService(apiUrl, data);
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode tree = mapper.readTree(result);

        @SuppressWarnings("unchecked")
        List<Object> sents = mapper.convertValue(tree.get("sentences"), List.class);

        List<SentenceScore> sentences = sents.stream().map(s->{
            @SuppressWarnings("unchecked")
            Map<String,Object> st = (Map<String,Object>)s;
            return new SentenceScore((String)st.get("sentence"), ((Double)st.get("score")).floatValue());
        }).collect(Collectors.toList());
        return sentences;
    }
    
}
