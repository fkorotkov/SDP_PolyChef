package ch.epfl.polychef;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import ch.epfl.polychef.users.ConnectedActivity;
import ch.epfl.polychef.users.User;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends ConnectedActivity {

    private DrawerLayout drawer;

    private NavController navController;
    private MenuItem currentItem;

    private User user;

    public static final String LOG_OUT = "Log out";
    private static final String TAG = "HomePage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        retrieveUserInfo(userEmail);

        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer);

        FragmentManager fragmentManager = getSupportFragmentManager();

        NavHostFragment hostFragment = (NavHostFragment)
                fragmentManager.findFragmentById(R.id.nav_host_fragment);

        navController = NavHostFragment.findNavController(hostFragment);

        // Create new Bundle containing the id of the container for the adapter
        Bundle bundle = new Bundle();
        bundle.putInt("fragmentID", R.id.nav_host_fragment);
        // Set this bundle to be an arguments of the startDestination using this trick
        navController.setGraph(R.navigation.nav_graph, bundle);
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button logButton = findViewById(R.id.logButton);
        logButton.setText(LOG_OUT);
        logButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private int getFragmentId(int itemId) {
        switch(itemId){
            case R.id.nav_home:
                return R.id.offlineMiniaturesFragment;
            case R.id.nav_fav:
                return R.id.favouritesFragment;
            case R.id.nav_subscribers:
                return R.id.subscribersFragment;
            case R.id.nav_subscriptions:
                return R.id.subscriptionsFragment;
            default:
                throw new IllegalArgumentException();
        }
    }
    
    private void setupNavigation(){
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem selectedItem) {

                        if (currentItem != null) {
                            currentItem.setChecked(false);
                        }

                        selectedItem.setChecked(true);
                        currentItem = selectedItem;

                        invalidateOptionsMenu();

                        int itemId = selectedItem.getItemId();

                        Bundle bundle = new Bundle();
                        bundle.putInt("fragmentID", R.id.nav_host_fragment);

                        navController.navigate(getFragmentId(itemId), bundle);

                        drawer.closeDrawer(GravityCompat.START, true);

                        return false;
                    }
                }
        );
    }

    private void retrieveUserInfo(String email) {

        Log.d(TAG, "Retrieving user info");

        FirebaseDatabase.getInstance()
                .getReference("users")
                .orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        long childrenCount = dataSnapshot.getChildrenCount();

                        if(childrenCount == 0) {
                            newUser(email);

                        } else if(childrenCount == 1) {
                            for(DataSnapshot child: dataSnapshot.getChildren()){
                                oldUser(child);
                            }

                        } else {
                            Log.e(TAG, "Inconsistent result: multiple user with the same email.", new IllegalStateException());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //TODO: Find good exception to throw
                        Log.e(TAG, "Query canceled", new IllegalArgumentException());
                    }
                });
    }

    private void newUser(String email) {
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        user = new User(email, username);

        //TODO: Integrate with the Firebase class
        //TODO: Add OnSuccess and OnFailure listener
        FirebaseDatabase.getInstance()
                .getReference("users")
                .push()
                .setValue(user);
    }

    private void oldUser(DataSnapshot snap){
        if(snap.exists()){
            user = snap.getValue(User.class);
        } else {
            //TODO: Find good exception to throw
            Log.e(TAG, "Unable to reconstruct the user from the JSON.", new IllegalStateException());
        }
    }
}
