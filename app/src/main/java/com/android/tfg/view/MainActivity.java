package com.android.tfg.view;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tfg.R;
import com.android.tfg.model.LoginUserModel;
import com.android.tfg.viewmodel.UserViewModel;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    LoginUserModel currentUser;
    View headerView;
    UserViewModel userViewModel;

    public void goHome(){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment, new HomeFragment());
        fragmentTransaction.commit();
    }

    public void goMap(){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment, new MapFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**************
         * VIEW MODEL *
         **************/
        initLoginUserViewModel();

        /***********
         * TOOLBAR *
         ***********/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**************
         * NAVIGATION *
         **************/
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        /*******************
         * MENU CAT. COLOR *
         *******************/
        Menu menu = navigationView.getMenu();
        MenuItem mGeneral = menu.findItem(R.id.menuGroupGeneral);
        SpannableString s = new SpannableString(mGeneral.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.MenuText), 0, s.length(), 0);
        mGeneral.setTitle(s);

        /*************
        * LOGIN DATA *
        **************/
        Intent i = getIntent();
        currentUser = (LoginUserModel) i.getSerializableExtra("currentUser");
        Log.v("LOL", currentUser.getMail());
        if(currentUser==null)   Log.v("LOL", "NULL");
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.welcomeMessage), Toast.LENGTH_LONG).show();
        headerView = navigationView.getHeaderView(0);
        TextView tHeaderMail = headerView.findViewById(R.id.tHeaderMail);
        tHeaderMail.setText(currentUser.getMail());

        /***********************
         * HOME FRAGMENT START *
         ***********************/
        goHome();

    }

    private void initLoginUserViewModel(){
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.option_menu, menu);
        /******************************
         * FIX COLOR (WHITE -> BLACK) *
         ******************************/
        for(int i=0; i<menu.size(); i++){
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // NAVEGACION
        switch(menuItem.getItemId()){
            case R.id.home: goHome(); break;
            case R.id.map: goMap(); break;
        }
        drawerLayout.closeDrawers(); // Cierra el menu lateral
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.optLogout:    userViewModel.logout();
                                    break;
        }
        return true;
    }
}
