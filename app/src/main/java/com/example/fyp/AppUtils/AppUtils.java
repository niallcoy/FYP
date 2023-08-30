package com.example.fyp.AppUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.fyp.MainActivity;
import com.example.fyp.Profile;
import com.example.fyp.R;
import com.example.fyp.SearchRecipe;
import com.example.fyp.WeekViewActivity;
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
        switch(item.getItemId()) {
            case R.id.logout:
                logout(activity);
                break;
            case R.id.searchRecipe:
                activity.startActivity(new Intent(activity, SearchRecipe.class));
                break;
            case R.id.weekView:
                activity.startActivity(new Intent(activity, WeekViewActivity.class));
                break;
            case R.id.profile:
                activity.startActivity(new Intent(activity, Profile.class));
                break;
            default:
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
