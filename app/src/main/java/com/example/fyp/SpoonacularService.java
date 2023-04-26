package com.example.fyp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



        public class SpoonacularService {
            private static final String API_KEY = "73e06ad04f4744af8036ab3d70c203ea";
            private Context context;

            public SpoonacularService(Context context) {
                this.context = context;
            }

            public void searchRecipesByCalories(int minCalories, int maxCalories, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
                // Construct the API URL with the given parameters
                String apiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + API_KEY
                        + "&minCalories=" + minCalories
                        + "&maxCalories=" + maxCalories
                        + "&type=breakfast"
                        + "&mealType=breakfast";// add this parameter to filter for breakfast recipes

                //API request
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null, listener, errorListener);

                // Add the request to the request queue
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
            public void getRecipeDetails(int id, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
                String url = "https://api.spoonacular.com/recipes/" + id + "/information?apiKey=" + API_KEY;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }


        }





