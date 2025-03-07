# importing the required libraries
import pandas as pd
import numpy as np
#import matplotlib.pyplot as plt
#import seaborn as sns
#from datetime import datetime as dt

import os
import warnings
warnings.filterwarnings('ignore')

#from wordcloud import WordCloud, STOPWORDS
#import re, unicodedata
#import string
import nltk
#import inflect
#from bs4 import BeautifulSoup
#from nltk import word_tokenize, sent_tokenize
#from nltk.corpus import stopwords
#from nltk.stem import LancasterStemmer, WordNetLemmatizer
import sklearn
from sklearn.feature_extraction.text import TfidfVectorizer
#from sklearn.decomposition import PCA

#from nltk.stem import WordNetLemmatizer
nltk.download('wordnet')
import nltk
nltk.download('stopwords')
#from nltk.corpus import stopwords
import string
string.punctuation
nltk.download('omw-1.4')
#from nltk.tokenize import TweetTokenizer

from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.cluster import AgglomerativeClustering
import scipy
#import scipy.cluster.hierarchy as shc
#from scipy import sparse

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel

import pickle 
from flask import Flask, request, jsonify
import firebase_admin
from firebase_admin import credentials, db

from scipy.sparse import csr_matrix
from scipy.sparse.linalg import svds

hybrid = Flask(__name__)

try:
    movie_df = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\content_based_dataset')
    indices = pd.Series(movie_df.index, index=movie_df['movieid']).drop_duplicates()
    # cf_preds_df = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\collaborative_user_prediction_dataset')
    user_ratings = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\Original_user_ratings')
    movies_cf = pd.read_csv(r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\collaborative_movie_dataset')
    firebase_url = 'https://recommender-system-53372-default-rtdb.asia-southeast1.firebasedatabase.app/'
    firebase_cred_path = r'C:\Users\LimWooiHong\OneDrive - unimap.edu.my\Uni Course\Y4SEM2\NMJ41904 FYP II\Machine Learning Code\recommender-system-53372-firebase-adminsdk-xu5ih-a96217d7a4.json'
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
        
class CFRecommender:
    
    MODEL_NAME = 'Collaborative Filtering'
    
    def __init__(self, user_ratings, movies_df, firebase_cred_path, firebase_db_url, num_factors=15):
        self.user_ratings = user_ratings
        self.movies_df = movies_df
        self.num_factors = num_factors
        
        # Initialize Firebase
        self.initialize_firebase(firebase_cred_path, firebase_db_url)
        
        # Retrieve the latest ratings from Firebase
        self.update_ratings_from_firebase()
        
        # Process the data
        self.process_data()
    
    def initialize_firebase(self, firebase_cred_path, firebase_db_url):
        # Initialize the Firebase app
        cred = credentials.Certificate(firebase_cred_path)
        firebase_admin.initialize_app(cred, {
            'databaseURL': firebase_db_url
        })
    
    def update_ratings_from_firebase(self):
        # Retrieve data from Firebase
        ref = db.reference('ratings')
        firebase_data = ref.get()
        
        # Convert the data to a DataFrame
        ratings_list = []
        for user_id, ratings in firebase_data.items():
            for movie_id, rating_data in ratings.items():
                ratings_list.append({
                    'userid': int(user_id),
                    'movieid': int(movie_id),
                    'rating': rating_data['rating']
                })
        
        firebase_df = pd.DataFrame(ratings_list)
        
        # Combine with existing ratings
        self.merge_user_ratings = pd.concat([self.user_ratings, firebase_df], ignore_index=True)
        
    def process_data(self):
        # Aggregate duplicates by taking the mean
        self.aggregated_ratings = self.merge_user_ratings.groupby(['userid', 'movieid'], as_index=False)['rating'].mean()
        
        # Pivot the DataFrame
        self.users_movies_pivot_matrix_df = self.aggregated_ratings.pivot(index='userid', 
                                                                          columns='movieid', 
                                                                          values='rating').fillna(0)
        
        # Create a sparse matrix
        self.users_movies_pivot_matrix = self.users_movies_pivot_matrix_df.values
        self.users_movies_pivot_sparse_matrix = csr_matrix(self.users_movies_pivot_matrix)
        
        # Perform matrix factorization
        self.U, self.sigma, self.Vt = svds(self.users_movies_pivot_sparse_matrix, k=self.num_factors)
        
        # Construct diagonal matrix for sigma
        self.sigma = np.diag(self.sigma)
        
        # Compute the predicted ratings
        self.all_user_predicted_ratings = np.dot(np.dot(self.U, self.sigma), self.Vt)
        
        # Normalize the predicted ratings
        self.all_user_predicted_ratings_norm = (self.all_user_predicted_ratings - self.all_user_predicted_ratings.min()) / (self.all_user_predicted_ratings.max() - self.all_user_predicted_ratings.min())
        
        # Create a DataFrame for the normalized predicted ratings
        self.cf_predictions_df = pd.DataFrame(self.all_user_predicted_ratings_norm, 
                                              columns=self.users_movies_pivot_matrix_df.columns, 
                                              index=self.users_movies_pivot_matrix_df.index)
        
        # Ensure movieid is the same type in both DataFrames
        self.cf_predictions_df.columns = self.cf_predictions_df.columns.astype(int)
        self.movies_df['movieid'] = self.movies_df['movieid'].astype(int)
        
    def get_model_name(self):
        return self.MODEL_NAME
        
    def recommend_items(self, user_id, top_n, verbose=True, movies_to_ignore=[], diversity_factor=0.1):
        if user_id not in self.cf_predictions_df.index:
            raise ValueError(f"User ID {user_id} not found in the dataset")
        
        # Get and sort the user's predictions
        user_predictions = self.cf_predictions_df.loc[user_id]

        user_predictions += diversity_factor * np.random.rand(len(user_predictions))
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
        
content_recommender = ContentBasedRecommender(movie_df, indices)
cf_recommender_model = CFRecommender(user_ratings, movies_cf, firebase_cred_path, firebase_url)

class HybridBasedRecommender:
    
    def __init__(self, content_recommender, cf_recommender_model):
        self.content_recommender = content_recommender
        self.cf_recommender_model = cf_recommender_model

    def get_hybrid_recommendations(self, user_id, product_id, top_n, similarity=True):
        content_based_recommendations = self.content_recommender.genre_recommendations(product_id, top_n, similarity)
        collaborative_filtering_recommendations = self.cf_recommender_model.recommend_items(user_id, top_n, verbose=True, diversity_factor = 0.1)
       
        # Merge and deduplicate recommendations using list method
        hybrid_recommendations_list = list(set(content_based_recommendations['Movie'].values)
                                           | set(collaborative_filtering_recommendations['title'].values))

        # Convert the list to a DataFrame
        hybrid_recommendations_df = pd.DataFrame(hybrid_recommendations_list, columns=['title']).head(top_n)

        hybrid_recommendations_df = hybrid_recommendations_df.merge(
            self.cf_recommender_model.movies_df,
            how='left',
            on='title'
            )[['title', 'movieid', 'genre', 'Poster', 'url']]
        return hybrid_recommendations_df


hybrid_recommender = HybridBasedRecommender(content_recommender, cf_recommender_model)

@hybrid.route('/hybrid_recommend', methods=['GET'])
def recommend():
    user_id = request.args.get('user_id', type=int)
    product_id = request.args.get('product_id', type=int)
    top_n = request.args.get('top_n', default=10, type=int)
    similarity = request.args.get('similarity', default=True, type=bool)

    if user_id is None or product_id is None:
        return jsonify({'error': 'user_id and product_id are required parameters'}), 400

    try:
        recommendations = hybrid_recommender.get_hybrid_recommendations(user_id, product_id, top_n, similarity)
        return jsonify(recommendations.to_dict(orient='records'))
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == "__main__":
    
    hybrid.run(host=os.getenv('IP', '0.0.0.0'), 
            port=int(os.getenv('PORT', 4444)),
           debug = True)