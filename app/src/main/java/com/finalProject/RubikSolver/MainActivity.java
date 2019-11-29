package com.finalProject.RubikSolver;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE = 233;

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

    /**Temp image file for taking photos. */
    private File tempFile;


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
     * @param position : a char that takes the position of user's tab.
     */
    private void imageClicked(Character position) {
        char[] temp = new char[6];
        switch (position) {
            case 'u':
                temp = u;
                break;
            case 'f':
                temp = f;
                break;
            case 'd':
                temp = d;
                break;
            case 'l':
                temp = l;
                break;
            case 'r':
                temp = r;
                break;
            case 'b':
                temp = b;
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
     * Uses https://github.com/yanzhenjie/AndPermission for permission control.
     * Save a temp pic file. See acknowledgement for details.
     */
    private void takePic() {
        AndPermission.with(this).runtime()
                .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(unused -> {
                    tempFile = new File(checkDirPath(Environment.getExternalStorageDirectory().getPath()
                            + "/" + getPackageName() + "/temp"), System.currentTimeMillis() + ".jpg");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        // Specify saving dir
                        // If statement used to handle different android version.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile));
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } else {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                        }
                        startActivityForResult(intent, REQUEST_CAPTURE);
                    }
                }).start();
    }

    /**
     * Check if the photo can be saved at a specific dir.
     * @param dirPath: the intended dir.
     * @return passed in dir or created dir.
     */
    private static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        // Create new directories.
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
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
                // If we have camera, go to camera page.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
                } else {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)));
                }
                //gotoClipActivity(Uri.fromFile(tempFile));
            }
        }
//            case REQUEST_CROP_PHOTO:  //剪切图片返回
//                if (resultCode == RESULT_OK) {
//                    final Uri uri = intent.getData();
//                    if (uri == null) {
//                        return;
//                    }
//                    String cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
//                    Bitmap bitMap = BitmapFactory.decodeFile(cropImagePath);
//
//                    imageViewList.get(position).setImageBitmap(bitMap);
//                    bitmapMap.put(position, bitMap);
//                    getColor();
//                }
//                break;
//        }
    }
}
