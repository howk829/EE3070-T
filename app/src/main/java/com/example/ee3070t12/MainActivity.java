package com.example.ee3070t12;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ee3070t12.Fragments.HomeFragment;
import com.example.ee3070t12.Fragments.SearchFragment;
import com.example.ee3070t12.Fragments.ShoplistFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    final Fragment homeFragment = new HomeFragment();
    final Fragment searchFragment = new SearchFragment();
    final Fragment shoplistFragment = new ShoplistFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = homeFragment;



    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignClient;

    public static FirebaseUser currentUser;

    public static FirebaseDatabase mFirebaseDatabase;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static DatabaseReference myRef;

    public static FirebaseFirestore fStore;
    public static String userID;


    public static Resources mResources;

    public static Calendar calendar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        fm.beginTransaction().add(R.id.fragment_container, searchFragment,"1").hide(searchFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, shoplistFragment,"2").hide(shoplistFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, homeFragment,"3").commit();

        mResources = getResources();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignClient = GoogleSignIn.getClient(this,gso);


        fStore = FirebaseFirestore.getInstance();

        calendar = Calendar.getInstance();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.login_icon){
            if(currentUser == null)
                signIn();
            else{
                Toast.makeText(MainActivity.this,"You have already logged in",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if(item.getItemId() == R.id.logout_icon){
            if(currentUser != null){
                mGoogleSignClient.signOut();
                currentUser = null;
                Toast.makeText(MainActivity.this,"You are logged out",Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signIn()
    {
        Intent signInIntent = mGoogleSignClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){

        try{
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Sign In Successfully",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        }
        catch(ApiException e){
            Toast.makeText(MainActivity.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }

    }


    private void FirebaseGoogleAuth(GoogleSignInAccount acc){
        try{
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(),null);
            mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        currentUser = mAuth.getCurrentUser();
                        updateUI(currentUser);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }



    private void updateUI(FirebaseUser user){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            userID = mAuth.getUid();

            Toast.makeText(MainActivity.this,"Welcome " + personName,Toast.LENGTH_SHORT).show();
        }

    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            fm.beginTransaction().hide(active).show(homeFragment).commit();
                            active = homeFragment;
                            return true;

                        case R.id.nav_search:
                            fm.beginTransaction().hide(active).show(searchFragment).commit();
                            active = searchFragment;
                            return true;

                        case R.id.nav_shoplist:
                            fm.beginTransaction().hide(active).show(shoplistFragment).commit();
                            active = shoplistFragment;
                            return true;
                    }
                    return false;
                }
            };

}
