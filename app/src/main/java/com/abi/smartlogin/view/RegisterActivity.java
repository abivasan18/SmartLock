package com.abi.smartlogin.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.abi.smartlogin.R;
import com.abi.smartlogin.db.DatabaseAccess;
import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.entity.UserRole;
import com.abi.smartlogin.util.Utility;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etUsername;
    private EditText etPassword;
    private ListView lstApps;
    private int selectedWallpaper = -1;
    private Wallpaper[] wallpapers;
    private DatabaseAccess databaseAccess;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.etUsername = (EditText) findViewById(R.id.etUsername);
        this.etPassword = (EditText) findViewById(R.id.etPassword);
        this.lstApps = (ListView) findViewById(R.id.lstApps);
        this.databaseAccess = DatabaseAccess.getInstance(getApplicationContext());


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.user = (User) bundle.get("user");
            this.etUsername.setEnabled(false);
            this.etUsername.setText(user.getUsername());
            this.etPassword.setText(user.getPassword());
            this.selectedWallpaper = user.getWallpaperId();
            Log.i(TAG, "Selected " + this.selectedWallpaper);
        } else {
            this.etUsername.setEnabled(true);
            this.etUsername.setText("");
            this.etPassword.setText("");
        }
        this.fillWallpapers();

    }

    public void save(View view) {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        boolean update = true;
        if (this.user == null) {
            update = false;
            this.user = new User();
            this.user.setUsername(username);
            this.user.setRole(UserRole.USER);
        }
        this.user.setPassword(password);
        this.user.setWallpaperId(this.selectedWallpaper);
        this.databaseAccess.open();
        if (update) {
            this.databaseAccess.update(this.user);
        } else {
            this.databaseAccess.insert(this.user);
        }
        this.databaseAccess.close();
        this.finish();
    }

    private void fillWallpapers() {
        Wallpaper[] wallpapers = {
                new Wallpaper("Home with balloons", R.drawable.wallpaper_1),
                new Wallpaper("Lake view", R.drawable.wallpaper_2),
                new Wallpaper("Mountain", R.drawable.wallpaper_3),
                new Wallpaper("Abstract", R.drawable.wallpaper_4),
                new Wallpaper("Vampire Hunter", R.drawable.wallpaper_5),
                new Wallpaper("River view", R.drawable.wallpaper_6),
        };
        this.wallpapers = wallpapers;
        if (selectedWallpaper == -1) {
            this.selectedWallpaper = wallpapers[0].id;
        }
        this.lstApps.setAdapter(new CustomAdaptor(this, wallpapers));
    }


    private class Wallpaper {
        private String name;
        private RoundedBitmapDrawable icon;
        private int id;

        public Wallpaper(String name, int iconId) {
            this.name = name;
            this.id = iconId;
//            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconId);
            icon = RoundedBitmapDrawableFactory.create(getResources(), Utility.decodeSampledBitmapFromResource(getResources(), iconId, 64, 64));
            //RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
            icon.setCircular(true);
            icon.setAntiAlias(true);
        }
    }

    private class CustomAdaptor extends ArrayAdapter<Wallpaper> {

        private Wallpaper[] wallpapers;
        private Switch[] switches;

        public CustomAdaptor(@NonNull Context context, Wallpaper[] wallpapers) {
            super(context, 0, wallpapers);
            this.wallpapers = wallpapers;
            this.switches = new Switch[wallpapers.length];
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final Wallpaper wallpaper = this.wallpapers[position];

            // Create convertView only for the first time
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_app, parent, false);

                convertView.findViewById(R.id.switchDisable).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Wallpaper wall = (Wallpaper) view.getTag();
                        selectedWallpaper = wall.id;
                        Switch switchDisable = (Switch) view;
                        for (Switch s : switches) {
                            if (s != null) {
                                s.setChecked(false);
                            }
                        }
                        switchDisable.setChecked(true);
                    }
                });
            }

            ImageView imgIcon = convertView.findViewById(R.id.imgIcon);
            TextView txtItem = convertView.findViewById(R.id.txtName);
            Switch switchDisable = convertView.findViewById(R.id.switchDisable);

            imgIcon.setImageDrawable(wallpaper.icon);
            txtItem.setText(wallpaper.name);
            switchDisable.setChecked(wallpaper.id == selectedWallpaper);
            switchDisable.setTag(wallpaper);

            this.switches[position] = switchDisable;

            return convertView;
        }
    }
}
