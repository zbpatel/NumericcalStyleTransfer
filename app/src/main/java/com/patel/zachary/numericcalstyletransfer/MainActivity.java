package com.patel.zachary.numericcalstyletransfer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// Numerical Edge SDK Imports:
import com.numericcal.dnn.Config;
import com.numericcal.dnn.Manager;
import com.numericcal.dnn.Handle;

import io.reactivex.Single;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String LOG_TAG = "NumericcalStyleTransfer";

    private int PICK_IMAGE_REQUEST_CODE = 1;

    // stores the image last selected by the user in way that is easily communicable between fns
    private Bitmap lastSelectedImage = null;

    Manager.Dnn netManager = null;

    // boolean to activate external storage related methods if permissions have been granted
    private boolean ENABLE_STORAGE_FUNCTIONS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Floating Button Pressed!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Adding buttons
        Button pickImageButton = (Button) findViewById(R.id.pickImageButton);
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageButtonClickHandler(view);
            }
        });

        Button applyFilterButton = (Button) findViewById(R.id.applyFilterButton);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFilterClickHandler(view);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // initializing netManager
        netManager = Manager.create(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Only remove the model on a pause that isn't a rotation change
        if (!isChangingConfigurations() && netManager != null) {
            netManager.release();
            netManager = null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // called after an activity launched by this activity (for a result) completes

        // choosing based on the code returned (which tells us which activity was completed)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri image_uri = data.getData();
            try {
                // transformation from content resolver to bitmap via: https://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                // this might cause issues with very large photos on machines with little RAM
                // if issues occur, add code to load bitmap in tiles
                Bitmap loadedImage = loadBitmap(image_uri);

                if (loadedImage == null) { throw new IOException("Bitmap has improper bounds."); }

                lastSelectedImage = loadedImage;
            } catch (IOException e) {
                e.printStackTrace();
                // pop a (short) toast saying that we were unable to load the image
                CharSequence error_text = "Unable to Load Image";
                Toast toast = Toast.makeText(getApplicationContext(), error_text, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    public void pickImageButtonClickHandler(View view) {
        // Handler for the pickImage button
        // on a press, prompts the user to select an image from their filesystem
        Log.i(LOG_TAG, "Started PickImageButtonHandler");

        Intent getImageIntent = new Intent();
        // Using an ACTION_GET_CONTENT intent with image type to select an image from gallery
        getImageIntent.setType("image/*");
        getImageIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(getImageIntent, "Select Picture"), PICK_IMAGE_REQUEST_CODE);

        Log.i(LOG_TAG, "Exiting PickImageButtonHandler");

    }

    public void applyFilterClickHandler(View view) {
        // Handler for the applyFilter button
        // on a press, applies the user selected filter to the user selected image
        Log.i(LOG_TAG, "Started applyFilterClickHandler");

        if (lastSelectedImage == null) {
            Snackbar.make(view, "Please pick an image first.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        } else {
            //TODO: start processing of image
            applyFilterToImage(lastSelectedImage);
        }

        Log.i(LOG_TAG, "Exiting applyFilterClickHandler");
    }

    private List<Object> getModelInfo(Single<Handle.Rx> modelDnn) {
        Single<Pair<Integer, Integer>> hw = modelDnn.map(handle -> new Pair<>(
                        handle.info.inputShape().get(1),
                        handle.info.inputShape().get(2)));
    }

    private Bitmap applyFilterToImage(Bitmap toTransform) {
        // Calls the necessary DNN code to execute a transformation of the provided image
        Log.i(LOG_TAG, "Started applyFilterToImage");

        Bitmap res = null;

        // Step 1: load input dimensions from model
        int inputH = toTransform.getWidth();
        int inputW = toTransform.getHeight();
        int modelH = 0;
        int modelW = 0;

        // Step 2: resize currently selected bitmap
        // set filter = true to get better image performance if upscaling
        Bitmap scaled = Bitmap.createScaledBitmap(toTransform, modelH, modelW, true);

        // Step 3: apply filter
        // TODO

        // Step 4: resize image back to original dimensions
        Bitmap res = Bitmap.createBitmap(res, inputH, inputW, true);

        Log.i(LOG_TAG, "Exiting applyFilterToImage");


        return res;
    }

    // Constant passed to the callback method for granting requests
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public void requestStoragePermissions() {
        // Checks if the user has granted this app permission to write to external storage in the
        // past. If it hasn't, then ask for permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "App requesting storage permissions from user.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // If the user has denied this permission in the past, provide a message explaining
                // why it is needed
                String explanationMsg = "This app needs permission to write to external storage in order to save photos to your device.";

                // prompting the user with a toast
                Toast.makeText(getApplicationContext(), explanationMsg, Toast.LENGTH_LONG).show();
            }

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            // if we reach this method, we know that write permissions have already been added so
            // enable storage functionality (which should already be true at this point)
            ENABLE_STORAGE_FUNCTIONS = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                          int[] grantResults) {
        // callback method for ActivityCompat.requestPermissions
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "App granted WRITE_EXTERNAL_STORAGE permissions.");
                    ENABLE_STORAGE_FUNCTIONS = true;
                } else {
                    Log.i(LOG_TAG, "App denied WRITE_EXTERNAL_STORAGE permissions.");
                    ENABLE_STORAGE_FUNCTIONS = false;
                }
            }
            // add additional cases if more permissions are needed in future
        }
    }
    
    public void storeImageExternal(Bitmap toStore) {
        if (ENABLE_STORAGE_FUNCTIONS) {
            // TODO: store the image to external storage (TBD)
        } else {
            // If storage functions are disabled, do nothing (perhaps prompt user for permissions)
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Single<Handle.Rx> configDnnHandle(String modelID) {
        Config.Model netCfg = Config.model(modelID).downloadUpdates(false).reportPerformance(false);
        return netManager.createHandle(netCfg);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //  ---- Misc. Helper Functions:
    private Bitmap loadBitmap(Uri bmpURI) throws IOException {
        // https://developer.android.com/topic/performance/graphics/load-bitmap
        // loads a bitmap from a URI
        // in order to be safe, this method "scans" the image twice, the first time looking to find the
        // size of the image (and throw an error if the size is weird) and then to actually load it
        Bitmap result = null;

        InputStream input_stream = this.getContentResolver().openInputStream(bmpURI);

        // using the Android BitmapFactory class to deal with loading bitmaps
        // first, configure some misc options (how we want the image to load)
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input_stream, null, boundsOptions);
        input_stream.close(); // requires that we throw an IOException

        // return null if image dimensions are weird
        if (boundsOptions.outHeight == -1 || boundsOptions.outWidth == -1) { return null; }

        // loading the full image
        BitmapFactory.Options loadOptions = new BitmapFactory.Options();

        // re-opening input stream
        input_stream = this.getContentResolver().openInputStream(bmpURI);
        result = BitmapFactory.decodeStream(input_stream, null, loadOptions);
        input_stream.close();
        return result;
    }
}
