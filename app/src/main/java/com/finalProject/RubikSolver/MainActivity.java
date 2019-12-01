package com.finalProject.RubikSolver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;


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
    private List<TextView> textViewList = new ArrayList<>(54);
    private Map<Integer, Bitmap> bitmapMap = new HashMap<>(6);
    private int centerPosition = 0;
    /** Solving fragment that handles all solving actions. */
    private Fragment solveTab;

    /** The arrayList that stores image view. */
    private HashMap<Character, ImageView> imageViewMap = new HashMap<>();

    /** Temp file path of the picture taken. */
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addView();
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
                centerPosition = 0;
                position = 'u';
                break;
            case 'f':
                temp = f;
                centerPosition = 2;
                position = 'f';
                break;
            case 'd':
                temp = d;
                centerPosition = 5;
                position = 'd';
                break;
            case 'l':
                temp = l;
                centerPosition = 1;
                position = 'l';
                break;
            case 'r':
                temp = r;
                centerPosition = 3;
                position = 'r';
                break;
            case 'b':
                temp = b;
                centerPosition = 4;
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
        AndPermission.with(this).runtime()
                .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(unused -> {
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
                                    "com.finalProject.RubikSolver.provider", photoFile);
                            takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePic, REQUEST_CAPTURE);
                        }
                    }
                }).start();
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
                bitmapMap.put(centerPosition, bitMap);
                getColor();
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


    private synchronized void getColor() {
        Bitmap bitmap = bitmapMap.get(centerPosition);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int p = 0;
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0:
                    p = bitmap.getPixel(width / 6, height / 6);
                    break;
                case 1:
                    p = bitmap.getPixel(width / 2, height / 6);
                    break;
                case 2:
                    p = bitmap.getPixel(width * 5 / 6, height / 6);
                    break;
                case 3:
                    p = bitmap.getPixel(width / 6, height / 2);
                    break;
                case 4:
                    p = bitmap.getPixel(width / 2, height / 2);
                    break;
                case 5:
                    p = bitmap.getPixel(width * 5 / 6, height / 2);
                    break;
                case 6:
                    p = bitmap.getPixel(width / 6, height * 5 / 6);
                    break;
                case 7:
                    p = bitmap.getPixel(width / 2, height * 5 / 6);
                    break;
                case 8:
                    p = bitmap.getPixel(width * 5 / 6, height * 5 / 6);
                    break;
            }
            textViewList.get(position * 9 + i).setText(new Pixel(Color.red(p), Color.green(p), Color.blue(p)).getColor());
        }
    }

    //Declaring views for showing the output result.
    @BindView(R.id.U1)
    TextView u1;
    @BindView(R.id.U2)
    TextView u2;
    @BindView(R.id.U3)
    TextView u3;
    @BindView(R.id.U4)
    TextView u4;
    @BindView(R.id.U5)
    TextView u5;
    @BindView(R.id.U6)
    TextView u6;
    @BindView(R.id.U7)
    TextView u7;
    @BindView(R.id.U8)
    TextView u8;
    @BindView(R.id.U9)
    TextView u9;

    @BindView(R.id.F1)
    TextView f1;
    @BindView(R.id.F2)
    TextView f2;
    @BindView(R.id.F3)
    TextView f3;
    @BindView(R.id.F4)
    TextView f4;
    @BindView(R.id.F5)
    TextView f5;
    @BindView(R.id.F6)
    TextView f6;
    @BindView(R.id.F7)
    TextView f7;
    @BindView(R.id.F8)
    TextView f8;
    @BindView(R.id.F9)
    TextView f9;

    @BindView(R.id.D1)
    TextView d1;
    @BindView(R.id.D2)
    TextView d2;
    @BindView(R.id.D3)
    TextView d3;
    @BindView(R.id.D4)
    TextView d4;
    @BindView(R.id.D5)
    TextView d5;
    @BindView(R.id.D6)
    TextView d6;
    @BindView(R.id.D7)
    TextView d7;
    @BindView(R.id.D8)
    TextView d8;
    @BindView(R.id.D9)
    TextView d9;

    @BindView(R.id.L1)
    TextView l1;
    @BindView(R.id.L2)
    TextView l2;
    @BindView(R.id.L3)
    TextView l3;
    @BindView(R.id.L4)
    TextView l4;
    @BindView(R.id.L5)
    TextView l5;
    @BindView(R.id.L6)
    TextView l6;
    @BindView(R.id.L7)
    TextView l7;
    @BindView(R.id.L8)
    TextView l8;
    @BindView(R.id.L9)
    TextView l9;

    @BindView(R.id.R1)
    TextView r1;
    @BindView(R.id.R2)
    TextView r2;
    @BindView(R.id.R3)
    TextView r3;
    @BindView(R.id.R4)
    TextView r4;
    @BindView(R.id.R5)
    TextView r5;
    @BindView(R.id.R6)
    TextView r6;
    @BindView(R.id.R7)
    TextView r7;
    @BindView(R.id.R8)
    TextView r8;
    @BindView(R.id.R9)
    TextView r9;

    @BindView(R.id.B1)
    TextView b1;
    @BindView(R.id.B2)
    TextView b2;
    @BindView(R.id.B3)
    TextView b3;
    @BindView(R.id.B4)
    TextView b4;
    @BindView(R.id.B5)
    TextView b5;
    @BindView(R.id.B6)
    TextView b6;
    @BindView(R.id.B7)
    TextView b7;
    @BindView(R.id.B8)
    TextView b8;
    @BindView(R.id.B9)
    TextView b9;

    public void addView() {
        textViewList.add(u1);
        textViewList.add(u2);
        textViewList.add(u3);
        textViewList.add(u4);
        textViewList.add(u5);
        textViewList.add(u6);
        textViewList.add(u7);
        textViewList.add(u8);
        textViewList.add(u9);

        textViewList.add(f1);
        textViewList.add(f2);
        textViewList.add(f3);
        textViewList.add(f4);
        textViewList.add(f5);
        textViewList.add(f6);
        textViewList.add(f7);
        textViewList.add(f8);
        textViewList.add(f9);

        textViewList.add(d1);
        textViewList.add(d2);
        textViewList.add(d3);
        textViewList.add(d4);
        textViewList.add(d5);
        textViewList.add(d6);
        textViewList.add(d7);
        textViewList.add(d8);
        textViewList.add(d9);

        textViewList.add(l1);
        textViewList.add(l2);
        textViewList.add(l3);
        textViewList.add(l4);
        textViewList.add(l5);
        textViewList.add(l6);
        textViewList.add(l7);
        textViewList.add(l8);
        textViewList.add(l9);

        textViewList.add(r1);
        textViewList.add(r2);
        textViewList.add(r3);
        textViewList.add(r4);
        textViewList.add(r5);
        textViewList.add(r6);
        textViewList.add(r7);
        textViewList.add(r8);
        textViewList.add(r9);

        textViewList.add(b1);
        textViewList.add(b2);
        textViewList.add(b3);
        textViewList.add(b4);
        textViewList.add(b5);
        textViewList.add(b6);
        textViewList.add(b7);
        textViewList.add(b8);
        textViewList.add(b9);
    }


}
