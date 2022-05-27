from transformers import pipeline
from sentence_transformers import SentenceTransformer, util
from nltk import sent_tokenize
import torch
def loadModels():
    #model = SentenceTransformer('paraphrase-MiniLM-L6-v2')
    models = {}

    #modelNames = ["paraphrase-MiniLM-L6-v2","models/all-distilroberta-v1-32","models/all-distilroberta-v1-64","/opensearch/models/msmarco-distilbert-cos-v5-64","/opensearch/models/all-MiniLM-L6-v2-64","/opensearch/models/multi-qa-MiniLM-L6-cos-v1-64"]
    modelNames = ["paraphrase-MiniLM-L6-v2"]
    for mdl in modelNames:
        models[mdl] = SentenceTransformer(mdl)

    return models

def getEmbedding(text, modelName, models):

    from sentence_transformers import SentenceTransformer
    model = models["paraphrase-MiniLM-L6-v2"]

    #Our sentences we like to encode
    sentences = [text]

    #Sentences are encoded by calling model.encode()
    embeddings = model.encode(sentences)
    return {"embedding":embeddings[0].tolist()}

def getSupportedEmbeddingModelsList():
    return [
        {"task":"embedding", "name":"bert-base-uncased","dim":768,"length":512}
        ,{"task":"embedding", "name":"roberta-base","dim":768,"length":512} 
        ,{"task":"embedding", "name":"facebook/bart-large","dim":1024,"length":1024}
        ,{"task":"embedding", "name":"sentence-transformers/all-mpnet-base-v2","dim":768,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/all-distilroberta-v1","dim":768,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2","dim":768,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/multi-qa-MiniLM-L6-cos-v1","dim":384,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L3-v2","dim":384,"length":128}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2","dim":384,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-tas-b","dim":768,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-v4","dim":768,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-MiniLM-L-6-v3","dim":384,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-distilroberta-base-v2","dim":768,"length":512}

        ,{"task":"embedding", "name":"sentence-transformers/all-mpnet-base-v2-32","dim":32,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/all-distilroberta-v1-32","dim":32,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2-32","dim":32,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/multi-qa-MiniLM-L6-cos-v1-32","dim":32,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L3-v2-32","dim":32,"length":128}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-32","dim":32,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-tas-b-32","dim":32,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-v4-32","dim":32,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-MiniLM-L-6-v3-32","dim":32,"length":512}

        ,{"task":"embedding", "name":"sentence-transformers/all-mpnet-base-v2-64","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/all-distilroberta-v1-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2-64","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2-64-finetuned","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/multi-qa-MiniLM-L6-cos-v1-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L3-v2-64","dim":64,"length":128}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-tas-b-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-v4-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-MiniLM-L-6-v3-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-distilroberta-base-v2-64","dim":64,"length":512}

        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-v4-128","dim":128,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-distilroberta-base-v2-128","dim":128,"length":512}

        ,{"task":"embedding", "name":"sentence-transformers/msmarco-distilbert-base-v4-256","dim":256,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-distilroberta-base-v2-256","dim":128,"length":512}

    ]

def getSupportedEmbeddingModelsList():
    return [
        {"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2","dim":768,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-32","dim":32,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-64","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-finetuned","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-finetuned-32","dim":32,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/paraphrase-MiniLM-L12-v2-finetuned-64","dim":64,"length":384}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2","dim":384,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-MiniLM-L-6-v3","dim":384,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/msmarco-MiniLM-L-6-v3-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/multi-qa-MiniLM-L6-cos-v1-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/multi-qa-MiniLM-L6-cos-v1","dim":384,"length":512}

    ]

def getSupportedEmbeddingModelsList():
    return [
        {"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2-64","dim":64,"length":512}
        ,{"task":"embedding", "name":"sentence-transformers/all-MiniLM-L12-v2","dim":384,"length":512}

    ]


def getSupportedQuestionsAnsweringModelsList():
    return [
        #{"task":"embedding", "name":"deepset/electra-base-squad2","length":384}
        #,{"task":"embedding", "name":"deepset/roberta-base-squad2","length":386}
        {"task":"embedding", "name":"distilbert-base-cased-distilled-squad","length":512}
        #,{"task":"embedding", "name":"mrm8488/longformer-base-4096-finetuned-squadv2","length":4096}
    ]

def loadEmbeddingModels(path):
    models = {}
    modelsList = getSupportedEmbeddingModelsList()
    for m in modelsList:
        name=m['name']
        if "sentence-transformers" in name:
            models[name] = SentenceTransformer(path+name)
        else:
            models[name] = loadHuggingfaceModel(path+name) #pipeline('feature-extraction', model=path+name)
    return models

def loadHuggingfaceModel(modelName):
    from transformers import AutoTokenizer, AutoModel
    tokenizer = AutoTokenizer.from_pretrained(modelName)
    model = AutoModel.from_pretrained(modelName)
    return [tokenizer,model]

def loadQuestionAnsweringModels(path):
    models = {}
    modelsList = getSupportedQuestionsAnsweringModelsList()
    for m in modelsList:
        name=m['name']
        models[name] = pipeline('question-answering', model=path+name)
    return models

def answer(model, question, context):
    result = model({'question':question, 'context':context})
    result['answer_highlight'] = highlight(result,context)
    return result

def highlight(result, context):
    mcontext = context[:result['end']] + "</em>" + context[result['end']:] 
    mcontext = mcontext[:result['start']] + "<em>" + mcontext[result['start']:] 
    sents = sent_tokenize(mcontext)
    sent = ''
    for i in range(0,len(sents)):
        if '<em>' in sents[i]:
            sent = sents[i]
            break
    return sent

def getTransformersEmbedding(model, text):
    return {"embedding":model(text)[0][0]} 

def getSbertEmbedding(model, text):
    print(model)
    embeddings = model.encode([text])
    return {"embedding":embeddings[0].tolist()}

def scoreSentences(model, text, context):
    sents = sent_tokenize(context)
    cembd = getEmbeddings(model,sents)
    tembd = getEmbeddings(model,[text])
    cosine_scores = util.cos_sim(tembd, cembd)
    #dot_scores = util.dot_score(tembd, cembd)
    sentsd = {}
    for x in zip(sents,cosine_scores.tolist()[0]):
        sentsd[x[1]] = x[0]
    return [ {"score":key,"sentence":sentsd[key]} for key]# in sorted(sentsd, reverse=True)] #{"sentence":sents[maxi],"score":maxval}

def getEmbeddings(model, texts):
    if 'SentenceTransformer' in str(model):
        return model.encode(texts).tolist()
    else:
        tokenizer = model[0]
        mdl = model[1]
        tokens = tokenizer(texts, padding=True, truncation=True, return_tensors="pt")
        output = mdl(**tokens)
        return output[0].tolist()[0]
        #return [torch.mean(torch.as_tensor(i), dim=1).tolist()[0] for i in model(texts)]
        #return [i[5][2] for i in model(texts)]

