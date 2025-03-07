# importing the required libraries
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime as dt

import os
import warnings
warnings.filterwarnings('ignore')

from wordcloud import WordCloud, STOPWORDS
import re, string, unicodedata
import nltk
#import inflect
from bs4 import BeautifulSoup
from nltk import word_tokenize, sent_tokenize
from nltk.corpus import stopwords
from nltk.stem import LancasterStemmer, WordNetLemmatizer
import sklearn
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.decomposition import PCA

from nltk.stem import WordNetLemmatizer
nltk.download('wordnet')
import nltk
nltk.download('stopwords')
from nltk.corpus import stopwords
import string
string.punctuation
nltk.download('omw-1.4')
from nltk.tokenize import TweetTokenizer

from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.cluster import AgglomerativeClustering
import scipy
import scipy.cluster.hierarchy as shc
from scipy import sparse

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel

import pickle 
from flask import Flask, request, jsonify



app = Flask(__name__)

# Load your movie data
try:
    movie_df = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\content_based_dataset')
    indices = pd.Series(movie_df.index, index=movie_df['movieid']).drop_duplicates()
except Exception as e:
    print(f"Error loading data: {e}")
    exit(1)

class ContentBasedRecommender:
    def __init__(self, movie_df, indices):
        self.movie_df = movie_df
        self.indices = indices
        genres_str = movie_df['genre'].str.split('|').astype(str)
        self.tfidf = TfidfVectorizer(analyzer='word', ngram_range=(1, 2), min_df=1)
        self.tfidf_matrix = self.tfidf.fit_transform(genres_str)
        self.cosine_sim = linear_kernel(self.tfidf_matrix, self.tfidf_matrix)

    def genre_recommendations(self, movie_id, top_n, similarity=False):
        if movie_id not in self.indices.index:
            return pd.DataFrame({'Movie': []})

        idx = self.indices[movie_id]
        sim_scores = list(enumerate(self.cosine_sim[idx]))
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        sim_scores = sim_scores[1:top_n+1]

        movie_indices = [i[0] for i in sim_scores]

        if not similarity:
            return pd.DataFrame({'Movie': self.movie_df['title'].iloc[movie_indices].values})
        else:
            similarity_ = [i[1] for i in sim_scores]
            return pd.DataFrame({'Movie': self.movie_df['title'].iloc[movie_indices].values, 'Similarity': similarity_})

# Load the model
content_recommender = ContentBasedRecommender(movie_df, indices)
pickle.dump(content_recommender, open("cb_model.pkl", 'wb'))


@app.route('/content_recommend', methods=['GET'])
def content_recommend():
    movie_id = int(request.args.get('movie_id'))
    top_n = int(request.args.get('top_n'))
    similarity = request.args.get('similarity', 'false').lower() == 'true'
    
    recommendations = content_recommender.genre_recommendations(movie_id, top_n, similarity)
    return recommendations.to_json(orient='records')

if __name__ == '__main__':
    ml_model = pickle.load(open("cb_model.pkl", "rb")) 
    app.run(host=os.getenv('IP', '0.0.0.0'), 
            port=int(os.getenv('PORT', 4444)),
           debug = True)
