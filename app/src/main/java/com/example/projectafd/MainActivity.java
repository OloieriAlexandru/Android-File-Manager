package com.example.projectafd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projectafd.fragments.HomeFragment;
import com.example.projectafd.fragments.StorageFragment;
import com.example.projectafd.utils.ToastBuilders;
import com.google.android.material.navigation.NavigationView;

import lombok.SneakyThrows;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;

    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initStoragePermissions();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();
        updateShownFragment(HomeFragment.class);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home_item:
                    updateShownFragment(HomeFragment.class);
                    break;
                case R.id.menu_main_storage_item:
                    updateShownFragment(StorageFragment.newInstance("/"));
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SneakyThrows
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    ToastBuilders.openShortToast(getApplicationContext(), "Permission denied! The app cannot access your file system without this permission!");
                    Thread.sleep(3000);
                    System.exit(1);
                }
            }
        }
        ToastBuilders.openShortToast(getApplicationContext(), "Permission was already granted! The app has everything it requires!");
    }

    public static void updateShownFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    public static void updateShownFragment(Class<? extends Fragment> clazz) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, clazz, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    public static void updateLastShownFragment() {
        fragmentManager.popBackStackImmediate();
    }

    private void initStoragePermissions() {
        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ToastBuilders.openShortToast(getApplicationContext(), "Permission was already granted! The app has everything it requires!");
        } else {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
