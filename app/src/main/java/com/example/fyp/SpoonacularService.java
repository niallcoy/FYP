package com.example.fyp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

public class SpoonacularService {
    private static final String API_KEY = "73e06ad04f4744af8036ab3d70c203ea";
    private Context context;

    public SpoonacularService(Context context) {
        this.context = context;
    }

    public void getRecipeDetailsByExtract(String recipeUrl, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        if (recipeUrl == null) {
            errorListener.onErrorResponse(new VolleyError("Recipe URL is null"));
            return;
        }
        String apiUrl = "https://api.spoonacular.com/recipes/extract?apiKey=" + API_KEY + "&url=" + recipeUrl;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null, listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    if (jsonResponse.has("nutrition")) {
                        JSONObject nutrition = jsonResponse.getJSONObject("nutrition");
                        JSONArray nutrients = nutrition.getJSONArray("nutrients");
                        for (int i = 0; i < nutrients.length(); i++) {
                            JSONObject nutrient = nutrients.getJSONObject(i);
                            if (nutrient.getString("title").equals("Calories")) {
                                String calories = nutrient.getString("amountMetric");
                                jsonResponse.put("calories", calories);
                                break;
                            }
                        }
                    }
                    return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void searchRecipesByCalories(int minCalories, int maxCalories, Response.Listener<JSONArray> successListener, Response.ErrorListener errorListener) {
        String url = "https://api.spoonacular.com/recipes/findByNutrients?minCalories=" + minCalories + "&maxCalories=" + maxCalories + "&type=breakfast&apiKey=" + API_KEY;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, successListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }



    public void getRecipeDetails(int id, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = "https://api.spoonacular.com/recipes/" + id + "/information?apiKey=" + API_KEY;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void searchAllRecipes(String query, Response.Listener<JSONArray> successListener, Response.ErrorListener errorListener) {
        String url = "https://api.spoonacular.com/recipes/complexSearch?query=" + query + "&apiKey=" + API_KEY + "&addRecipeInformation=true";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, successListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
    public void onResponse(JSONArray response) {
        Log.d("API_RESPONSE", "Got Response: " + response.toString());

    }

    public void onErrorResponse(VolleyError error) {
        Log.e("API_ERROR", "Error: " + error.toString());

    }



}