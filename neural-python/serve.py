from neural_services import answer, getEmbeddings, getSbertEmbedding, loadEmbeddingModels, loadModels, loadQuestionAnsweringModels, scoreSentences
from neural_services import getEmbedding
path = 'models/'
embeddingModels = loadEmbeddingModels(path)
qaModels = loadQuestionAnsweringModels(path)
qaDefaultModel = "distilbert-base-cased-distilled-squad"
embeddingDefaultModel = "sentence-transformers/all-MiniLM-L12-v2-64"
#embeddingDefaultModel = "roberta-base"
#models = loadModels()
#print(models)
#print(getEmbedding('hello world','paraphrase-MiniLM-L6-v2',models))
#from transformers import pipeline
#question_answerer = pipeline("question-answering")

from fastapi import FastAPI

app = FastAPI()

from pydantic import BaseModel


class RequestParam(BaseModel):
    text: str
    model: str

class QuestionAnswerParam(BaseModel):
    question: str
    context: str
    model: str

class SentencesParam(BaseModel):
    text: str
    context: str
    model: str


@app.post("/embedding")
async def embedding(param:RequestParam):
    modelName = param.model
    if(modelName == "default"):
        modelName = embeddingDefaultModel
    print(modelName, param.model, embeddingDefaultModel)
    model = embeddingModels[modelName]
    return {"embedding":getEmbeddings(model, [param.text])[0]}
    #if("sentence-transformers" in modelName):
    #    return getSbertEmbedding(model,param.text)
    #else:
    #    return getEmbedding(model,param.text)

@app.post("/sentences")
async def sentences(param:SentencesParam):
    modelName = param.model
    if(modelName == "default"):
        modelName = embeddingDefaultModel
    model = embeddingModels[modelName]
    return {"sentences":scoreSentences(model, param.text,param.context)}

@app.post("/qa")
async def answer_question(param:QuestionAnswerParam):
    modelName = param.model
    if(modelName == "default"):
        modelName = qaDefaultModel
    model = qaModels[modelName]
    return answer(model, param.question,param.context)

@app.get("/")
async def root():
    return {"status":"active"}