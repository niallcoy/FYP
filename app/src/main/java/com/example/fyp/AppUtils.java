package com.example.fyp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.fyp.MainActivity;
import com.example.fyp.Profile;
import com.example.fyp.R;
import com.example.fyp.SearchRecipe;
import com.example.fyp.WeekViewActivity;
import com.example.fyp.Progress;
import com.google.firebase.auth.FirebaseAuth;

public class AppUtils {

    public static void logout(Activity activity) {
        FirebaseAuth.getInstance().signOut(); // Sign out the user
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
        activity.startActivity(intent);
        activity.finish(); // Finish the current activity
    }

    public static void handleMenuItemClick(MenuItem item, Activity activity) {
        Log.d("AppUtils", "handleMenuItemClick: Clicked item with id " + item.getItemId()); // Log item id

        switch(item.getItemId()) {
            case R.id.logout:
                Log.d("AppUtils", "handleMenuItemClick: Logout selected");
                logout(activity);
                break;
            case R.id.searchRecipe:
                Log.d("AppUtils", "handleMenuItemClick: SearchRecipe selected");
                activity.startActivity(new Intent(activity, SearchRecipe.class));
                break;
            case R.id.weekView:
                Log.d("AppUtils", "handleMenuItemClick: WeekView selected");
                activity.startActivity(new Intent(activity, WeekViewActivity.class));
                break;
            case R.id.profile:
                Log.d("AppUtils", "handleMenuItemClick: Profile selected");
                activity.startActivity(new Intent(activity, Profile.class));
                break;
            case R.id.progress:
                Log.d("AppUtils", "handleMenuItemClick: Progress selected");
                activity.startActivity(new Intent(activity, Progress.class));
                break;
            case R.id.favourites:
                Log.d("AppUtils", "handleMenuItemClick: ViewFavourites selected");
                activity.startActivity(new Intent(activity, ViewFavourites.class));
                break;
            default:
                Log.d("AppUtils", "handleMenuItemClick: Unknown item selected");
                break;
        }
    }

    public static void showPopUp(Activity activity, View v, PopupMenu.OnMenuItemClickListener listener) {
        PopupMenu popUp = new PopupMenu(activity, v);
        popUp.setOnMenuItemClickListener(listener);
        popUp.inflate(R.menu.popup_menu);
        popUp.show();
    }
}
