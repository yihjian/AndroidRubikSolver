package com.finalProject.RubikSolver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.finalProject.RubikSolver.min2phase.Search;
import com.finalProject.RubikSolver.min2phase.Tools;
import com.finalProject.RubikSolver.ui.main.SectionsPagerAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE = 233;
    private static final int REQUEST_CROP_PHOTO = 998;
    private char position;
    /** Char arrays that stores the color of each face.
     *  !!!!!!!!!!!!!!!!!!!!!!!
     *  Use lowercase char for color.
     *  w for white
     *  y for yellow
     *  b for blue
     *  g for green
     *  r for red
     *  o for orange
     *  !!!!!!!!!!!!!!!!!!!!!!
     */
    private char[] u = new char[9];
    private char[] f = new char[9];
    private char[] d = new char[9];
    private char[] l = new char[9];
    private char[] r = new char[9];
    private char[] b = new char[9];

    /** Solving fragment that handles all solving actions. */
    private Fragment solveTab;

    /** The arrayList that stores image view. */
    private HashMap<Character, ImageView> imageViewMap = new HashMap<>();

    /** Temp file path of the picture taken. */
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the two tab layout.
        setContentView(R.layout.activity_main);
        final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //Fill the char array with 'n' initially.
        Arrays.fill(u, 'n');
        Arrays.fill(f, 'n');
        Arrays.fill(d, 'n');
        Arrays.fill(l, 'n');
        Arrays.fill(r, 'n');
        Arrays.fill(b, 'n');

        // This doesn't work for some reason. Should be the same logic as the listener.
//        try {
//            solveTab = sectionsPagerAdapter.getItem(0);
//            Log.d("fragment", solveTab.toString());
//            View a = solveTab.getView();
//            Log.d("fragment", a.toString());
//        } catch (NullPointerException e) {
//            Log.d("nullpointer", "Strange null pointer case");
//        }

        //Set on select listener for tabs. When solving tab is selected, start solving process.
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // If solving tab got selected, start the solving process.
                int position = tab.getPosition();
                if (position == 1) {
                    solveTab = sectionsPagerAdapter.getItem(position);
                    handleSolvingAction(solveTab);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * This function handles the solving activity.
     * It connects the functions of taking photo and outputting result.
     * @param fragment: the solving fragment tab.
     */
    private void handleSolvingAction(Fragment fragment) {
        //Log.d("textbox is ", a.getText().toString());

        Button solve = fragment.getView().findViewById(R.id.solve);
        Button clear = fragment.getView().findViewById(R.id.clear);

        //Add each imageView to HashMap. Then use a for loop to setup onclickListener.
        imageViewMap.put('u', fragment.getView().findViewById(R.id.uIv));
        imageViewMap.put('f', fragment.getView().findViewById(R.id.fIv));
        imageViewMap.put('d', fragment.getView().findViewById(R.id.dIv));
        imageViewMap.put('l', fragment.getView().findViewById(R.id.lIv));
        imageViewMap.put('r', fragment.getView().findViewById(R.id.rIv));
        imageViewMap.put('b', fragment.getView().findViewById(R.id.bIv));
        for(Map.Entry<Character, ImageView> entry : imageViewMap.entrySet()) {
            entry.getValue().setOnClickListener(unused -> imageClicked(entry.getKey()));
        }


        clear.setOnClickListener(unused -> clearInput());
        solve.setOnClickListener(unused -> solveClicked());


    }

    /**
     * This function is called when "solving is clicked"
     * Pops out an alert dialog that shows the result.
     * Min2Phase algorithm referenced from https://github.com/cs0x7f/min2phase.
     */
    private void solveClicked() {
        Log.d("clicking solved ", "Solving");

        // Inflate view for later use.
        View solutionDialog = getLayoutInflater()
                .inflate(R.layout.chunk_solution, null, false);
        TextView solution = solutionDialog.findViewById(R.id.algorithm);

        /* This shit is slow af, using different testing algorithm for now
        // !TODO remove this after color recognizing is done.
        // Testing rubik faces. Generated by "F U' F2 D' B U R' F' L D' R' U' L U B' D2 R' F U2 D2"
        // should return FBLLURRFBUUFBRFDDFUULLFRDDLRFBLDRFBLUUBFLBDDBUURRBLDDR after conversion.
        u = new char[]{'g', 'b', 'o', 'o', 'w', 'r', 'r', 'g', 'b'};
        f = new char[]{'w', 'w', 'o', 'o', 'g', 'r', 'y', 'y', 'o'};
        d = new char[]{'r', 'g', 'b', 'o', 'y', 'r', 'g', 'b', 'o'};
        l = new char[]{'w', 'w', 'b', 'g', 'o', 'b', 'y', 'y', 'b'};
        r = new char[]{'w', 'w', 'g', 'b', 'r', 'g', 'y', 'y', 'g'};
        b = new char[]{'w', 'w', 'r', 'r', 'b', 'o', 'y', 'y', 'r'};

        // Convert traditional RGB chars to to UBL definition used by min2phase
        String rgbCube = new String(u) + new String(r) + new String(f) + new String(d) + new String(l) + new String(b);
        Log.d("cube before switching: ", rgbCube);
        rgbCube = rgbCube.replace(u[4], 'U');
        rgbCube = rgbCube.replace(r[4], 'R');
        rgbCube = rgbCube.replace(f[4], 'F');
        rgbCube = rgbCube.replace(d[4], 'D');
        rgbCube = rgbCube.replace(l[4], 'L');
        rgbCube = rgbCube.replace(b[4], 'B');
        Log.d("Cube after switching: ", rgbCube);
        */

        //String rgbCube = "DUUBULDBFRBFRRULLLBRDFFFBLURDBFDFDRFRULBLUFDURRBLBDUDL";
        String rgbCube = Tools.randomCube();
        //Test: solution.setText("F D F' U R' L' F D' U R D' F' D2 R U2");
        //Test: Log.d("Conversion from min2phase package is: ", Tools.fromScramble("F U' F2 D' B U R' F' L D' R' U' L U B' D2 R' F U2 D2"));
        //Use min2phase to search for solution.
        Search.init();
        String result = new Search().solution(rgbCube, 21, 100000000, 0, 0);
        solution.setText(result);
        Log.d("Result", result);

        // First hard-code URL-base for final animation.
        // Use for loop to append algorithm
        String urlBase = "https://alg.cubing.net/?";
        String setUp = "setup=";
        String algorithm = "%0A&alg=";
        String convert = result.replace('\'', '-');
        String[] convertToURL = convert.split(" ");
        for (int i = convertToURL.length - 1; i >= 0; i--) {
            String temp = convertToURL[i];
            if (temp.length() == 2 && temp.charAt(1) == '-') {
                temp = temp.substring(0,1);
            } else if (temp.length() == 1) {
                temp = temp + "-";
            }
            setUp = setUp + temp + "_";
        }
        for (int i = 0; i < convertToURL.length; i++) {
            algorithm = algorithm + convertToURL[i].trim() + "_";
        }
        String url = urlBase + setUp + algorithm;

        //Pop an alert Dialog that shows solution.
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(solutionDialog)
                .setPositiveButton(R.string.seeAnimation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Open a webpage if user want to see animation.
                        // Currently using web browser. !TODO We could update it with an curl maybe?
                        // Uri webpage = Uri.parse("https://alg.cubing.net/?setup=U2_R-_D2_F_D_R-_U-_D_F-_L_R_U-_F_D-_F-%0A&alg=F_D_F-_U_R-_L-_F_D-_U_R_D-_F-_D2_R_U2");
                        Log.d("URL", url);
                        Uri webpage = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing when dismiss.
                    }
                });
        builder.create().show();
    }

    private void clearInput() {
        Log.d("Clearing input clicked: ", "not doing anything yet");
    }

    /** Called when user tabs specific block to take picture of cube.
     *  Should take photo - read color - and update stored color in array.
     * @param p : a char that takes the position of user's tab.
     */
    private void imageClicked(Character p) {
        char[] temp = new char[6];
        switch (p) {
            case 'u':
                temp = u;
                position = 'u';
                break;
            case 'f':
                temp = f;
                position = 'f';
                break;
            case 'd':
                temp = d;
                position = 'd';
                break;
            case 'l':
                temp = l;
                position = 'l';
                break;
            case 'r':
                temp = r;
                position = 'r';
                break;
            case 'b':
                temp = b;
                position = 'b';
                break;
            default:
                Log.d("Bad input from image onclickListener", Character.toString(position));
        }

        /*
        Arrays.fill(temp, 't');
        Log.d("u is", Arrays.toString(u));
        Log.d("d is", Arrays.toString(d));
        */
        takePic();
    }

    /**
     * The function that handles picture taking.
     * Save a temp pic file.
     */
    private void takePic() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("fuck ", ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.finalProject.RubikSolver.provider",
                        photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, REQUEST_CAPTURE);
            }
        }
    }

    /**
     * This function creates a temp file that stores the taken image.
     * Also stores a path for later use. Taken from Google tutorial.
     * @return a File that represent the temporary position of photo.
     * @throws IOException if we cannot make files, throw an IO exception.
     */
    private File createImageFile() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "JPEG_" + time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                name,
                ".jpg",
                dir
                );
        path = image.getAbsolutePath();
        return image;
    }

    /**
     * This function calls the camera api or reads the file.
     * @param requestCode: calling camera action or reading file.
     * @param resultCode: OK or not OK.
     * @param intent which intent are we asking for result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CAPTURE) { //requesting camera.
            if (resultCode == RESULT_OK) {
                clipPic(Uri.parse(path));
            } else {
                Toast.makeText(this, "Camera cancelled ", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CROP_PHOTO){  //when receive cropped image
            Log.d("cropping", "???");
            if (resultCode == RESULT_OK) {
                final Uri uri = intent.getData();
                if (uri == null) {
                    return;
                }
                String cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
                Bitmap bitMap = BitmapFactory.decodeFile(cropImagePath);
                Log.d("Current selected position ", Character.toString(position));
                imageViewMap.get(position).setImageBitmap(bitMap);
//                getColor();
            }
        }
    }

    /*
    The clipping activities are integrated from https://github.com/yihjian/MagicCube.
    I hasn't gone through them yet. Seems to be working until someone found bugs.
     */


    private void clipPic(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Could not load picture", Toast.LENGTH_LONG).show();
        }
//        Log.d("sending intent to handle pic ", uri.toString());
        Intent intent = new Intent();
        intent.setClass(this, ClipImageActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
