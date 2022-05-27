package org.neural.search.opensearch.plugins.highlight;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.neural.search.opensearch.plugins.Helper;
import org.neural.search.opensearch.plugins.NeuralQuery;
import org.neural.search.opensearch.plugins.NeuralQueryParams;
import org.neural.search.opensearch.plugins.services.NeuralAnswer;
import org.neural.search.opensearch.plugins.services.NeuralException;
import org.neural.search.opensearch.plugins.services.SentenceScore;
import org.opensearch.common.text.Text;
import org.opensearch.index.mapper.MappedFieldType;
import org.opensearch.search.fetch.subphase.highlight.FieldHighlightContext;
import org.opensearch.search.fetch.subphase.highlight.HighlightField;
import org.opensearch.search.fetch.subphase.highlight.Highlighter;
import org.opensearch.search.fetch.subphase.highlight.SearchHighlightContext;
import org.opensearch.search.fetch.subphase.highlight.UnifiedHighlighter;
import org.opensearch.search.fetch.subphase.highlight.SearchHighlightContext.FieldOptions;
import org.opensearch.search.lookup.SourceLookup;

public class NeuralHighlighter implements Highlighter {
    public static final String NAME = "neural";
    public final String MODEL_NAME_OPTION = "model";
    //public final String K_OPTION = "k";
    public final String HIGHLIGHT_TYPE_OPTION = "highlight_type";
    public final String HIGHLIGHT_TAG_OPTION = "highlight_tag";
    public final String SHOW_SCORE_OPTION = "show_score";
    public final String SCORE_TAG_OPTION = "score_tag";

    static List<NeuralQuery> locateKNNQuery(Query query) {
        if (query == null) {
            return Collections.emptyList();
        }
        List<NeuralQuery> queries = new ArrayList<>();
        query.visit(new QueryVisitor() {
            @Override
            public void visitLeaf(Query query) {
                if (query instanceof NeuralQuery) {
                    queries.add((NeuralQuery) query);
                }
            }
        });
        return queries;
    }

    @Override
    public HighlightField highlight(FieldHighlightContext fieldContext) {
        List<Text> responses = new ArrayList<>();
        try {
            List<NeuralQuery> queries = locateKNNQuery(fieldContext.context.query());
            if (queries.size() > 0) {
                NeuralQueryParams queryParams = queries.get(0).getParams();
                SourceLookup sourceLookup = fieldContext.hitContext.sourceLookup();
                Map<String, Object> source = sourceLookup.loadSourceIfNeeded();
                Object value = source.get(fieldContext.fieldName);
                String modelName = "default";
                String highlightType = "qa";
                String highlightTag = "em";
                Boolean showScore = false;
                String scoreTag = "score";
                
                FieldOptions fieldOptions = fieldContext.field.fieldOptions();
                if (fieldOptions != null) {
                    Map<String, Object> options = fieldContext.field.fieldOptions().options();
                    if(options!=null)
                    {
                        if(options.get(MODEL_NAME_OPTION) instanceof String)
                        modelName = (String) options.get(MODEL_NAME_OPTION);
                        modelName = modelName==null?"default":modelName;
                        //k = (Integer) options.get(K_OPTION);
                        if(options.get(HIGHLIGHT_TYPE_OPTION) instanceof String)
                        highlightType = (String) options.get(HIGHLIGHT_TYPE_OPTION);
                        highlightType = highlightType==null?"qa":highlightType;
    
                        if(options.get(HIGHLIGHT_TAG_OPTION) instanceof String)
                        highlightTag = (String) options.get(HIGHLIGHT_TAG_OPTION);
                        highlightTag = highlightTag==null?"em":highlightTag;
    
                        if(options.get(SCORE_TAG_OPTION) instanceof String)
                        scoreTag = (String) options.get(SCORE_TAG_OPTION);
                        scoreTag = scoreTag==null?"score":scoreTag;
    
                        if(options.get(SHOW_SCORE_OPTION) instanceof Boolean)
                        showScore = (Boolean) options.get(SHOW_SCORE_OPTION);
                        showScore = showScore==null?false:showScore;
                    }
                }

                NeuralAnswer answer = null;
                if (highlightType.equals("qa")) {

                    String fieldValue = (String)value;
                    if (fieldValue.trim().length() > 0)
                        answer = Helper.answer(queryParams.getText(), fieldValue, modelName);
                    if (answer != null) {
                        String text = answer.getAnswerHighlight();
                        text = text.replace("<em>", "<"+highlightTag+">");
                        if(showScore)
                        {
                            String scorePreTag = "<"+scoreTag+">";
                            String scorePostTag = "</"+scoreTag+">";
                            String fullScore=scorePreTag+"("+String.format("%.2f", answer.getScore())+")"+scorePostTag;
                            text = text.replace("</em>",fullScore+ "</"+highlightTag+">");
                        }
                        else
                        text = text.replace("</em>", "</"+highlightTag+">");
                        responses.add(new Text(text));
                    } 
                } else if (highlightType.equals("sentence")) {
                    String fieldValue = (String)value;
                    List<SentenceScore> sents = null;
                    if (fieldValue.trim().length() > 0)
                        sents = Helper.scoreSentences(queryParams.getText(), fieldValue, modelName);

                    if (sents != null) {
                        int ind = 0;
                        float score = -999999;
                        for(int i=0; i<sents.size();i++)
                        {
                            if(score<sents.get(i).getScore())
                            {
                                score = sents.get(i).getScore();
                                ind = i;
                            }
                            
                        }

                        String sentHighlight = "";
                        sentHighlight = "<"+highlightTag+">"+sents.get(ind).getSentence();
                        if(showScore)
                        {
                            String scorePreTag = "<"+scoreTag+">";
                            String scorePostTag = "</"+scoreTag+">";
                            String fullScore=scorePreTag+"("+String.format("%.2f", sents.get(ind).getScore())+")"+scorePostTag;
                            sentHighlight += fullScore+"</"+highlightTag+">";
                        }
                        else
                        sentHighlight += "</"+highlightTag+">";

                        if(sents.size()-1>ind)
                        sentHighlight+=" "+sents.get(ind+1).getSentence();

                        if(ind-1>0)
                        sentHighlight+=" "+sents.get(ind-1).getSentence();

                        responses.add(new Text(sentHighlight));

                    }

                }
            }

            return new HighlightField(fieldContext.fieldName, responses.toArray(new Text[] {}));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println(sw.toString());

        }

        return new HighlightField(fieldContext.fieldName, responses.toArray(new Text[] {}));
    }

    @Override
    public boolean canHighlight(MappedFieldType fieldType) {
        return true;
    }

    private static class CacheEntry {
        private int position;
        private int docId;
    }

}
