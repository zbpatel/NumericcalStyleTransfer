package com.patel.zachary.numericcalstyletransfer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String LOG_TAG = "NumericcalStyleTransfer";

    private int PICK_IMAGE_REQUEST_CODE = 1;

    // stores the image last selected by the user in way that is easily communicable between fns
    private Bitmap lastSelectedImage = null;

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

        }

        Log.i(LOG_TAG, "Exiting applyFilterClickHandler");
    }


    public void applyFilterToImage(Bitmap toTransform) {
        // Calls the necessary DNN code to execute a transformation of the provided image
        Log.i(LOG_TAG, "Started applyFilterToImage");

        // TODO: add code to transform the image

        Log.i(LOG_TAG, "Exiting applyFilterToImage");
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
    private Bitmap loadBitmap(Uri bmp_uri) throws IOException {
        // loads a bitmap from a URI
        // in order to be safe, this method "scans" the image twice, the first time looking to find the
        // size of the image (and throw an error if the size is weird) and then to actually load it
        Bitmap result = null;

        InputStream input_stream = this.getContentResolver().openInputStream(bmp_uri);

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
        input_stream = this.getContentResolver().openInputStream(bmp_uri);
        result = BitmapFactory.decodeStream(input_stream, null, loadOptions);
        input_stream.close();
        return result;
    }
}
