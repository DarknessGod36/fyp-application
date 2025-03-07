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

collaborative = Flask(__name__)

# Load your movie data
try:
    cf_preds_df = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\collaborative_user_prediction_dataset')
    movies_cf = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\collaborative_movie_dataset')
except Exception as e:
    print(f"Error loading data: {e}")
    exit(1)

class CFRecommender:
    
    MODEL_NAME = 'Collaborative Filtering'
    
    def __init__(self, cf_predictions_df, movies_df):
        # Remove or rename the 'Unnamed: 0' column if it exists
        if 'Unnamed: 0' in cf_predictions_df.columns:
            cf_predictions_df = cf_predictions_df.drop(columns=['Unnamed: 0'])
        self.cf_predictions_df = cf_predictions_df
        self.movies_df = movies_df
         # Ensure movieid is the same type in both DataFrames
        self.cf_predictions_df.columns = self.cf_predictions_df.columns.astype(int)
        self.movies_df['movieid'] = self.movies_df['movieid'].astype(int)
        
    def get_model_name(self):
        return self.MODEL_NAME
        
    def recommend_items(self, user_id, top_n, verbose=True, movies_to_ignore=[]):

        if user_id not in self.cf_predictions_df.index:
            raise ValueError(f"User ID {user_id} not found in the dataset")
        
        # Get and sort the user's predictions
        user_predictions = self.cf_predictions_df.loc[user_id]
        sorted_user_predictions = user_predictions.sort_values(ascending=False) \
                                    .reset_index().rename(columns={user_id: 'recStrength', 'index': 'movieid'})

        # Recommend the highest predicted rating movies that the user hasn't seen yet.
        recommendations_df = sorted_user_predictions[~sorted_user_predictions['movieid'].isin(movies_to_ignore)] \
                               .sort_values('recStrength', ascending = False) \
                               .head(top_n)

        if verbose:
            if self.movies_df is None:
                raise Exception('"movies_df" is required in verbose mode')

            recommendations_df = recommendations_df.merge(self.movies_df, how = 'left', 
                                                          left_on = 'movieid', 
                                                          right_on = 'movieid')[['recStrength', 'movieid', 'title', 'genre']]


        return recommendations_df
    
# Create an instance of CFRecommender
cf_recommender_model = CFRecommender(cf_preds_df, movies_cf)
pickle.dump(cf_recommender_model, open("cf_model.pkl", 'wb'))


# loading model from file system 
@collaborative.route('/collaborative_recommend', methods=['GET'])
def collaborative_recommend():
    user_id = int(request.args.get('user_id'))
    top_n = int(request.args.get('top_n'))
    verbose = request.args.get('verbose', 'false').lower() == 'true'
    
    recommendations = cf_recommender_model.recommend_items(user_id, top_n, verbose)
    return recommendations.to_json(orient='records')

if __name__ == '__main__':
    # using "wb" will overwrite the existing pickled file. so opening it in read-only mode("rb") 
    ml_model = pickle.load(open("cf_model.pkl", "rb")) 
    collaborative.run(host=os.getenv('IP', '0.0.0.0'), 
                    port=int(os.getenv('PORT', 4444)),
                    debug = True)
