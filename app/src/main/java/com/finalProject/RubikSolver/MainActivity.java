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
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static java.lang.Character.toLowerCase;


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
    char[] temp = new char[6];
    private char[] u = new char[9];
    private char[] f = new char[9];
    private char[] d = new char[9];
    private char[] l = new char[9];
    private char[] r = new char[9];
    private char[] b = new char[9];
    private List<EditText> textViewList = new ArrayList<>(54);
    private Map<Integer, Bitmap> bitmapMap = new HashMap<>(6);
    private int centerPosition = 0;
    protected Unbinder unbinder;
    /** Solving fragment that handles all solving actions. */
    private Fragment solveTab;
    private String m_Text = "";
    /** The arrayList that stores image view. */
    private HashMap<Character, ImageView> imageViewMap = new HashMap<>();

    /** Temp file path of the picture taken. */
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the two tab layout.
        setContentView(R.layout.activity_main);
        //unbinder = ButterKnife.bind(this);
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
        initilizeCenterPiece();

        // This doesn't work for some reason. Should be the same logic as the listener.
//        try {
//            solveTab = sectionsPagerAdapter.getItem(1);
//            Log.d("fragment", solveTab.toString());
//            View a = solveTab.getView();
//            Log.d("fragment", a.toString());
//            handleSolvingAction(solveTab);
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
                    Log.d("in handle ", solveTab.toString());
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

    /*@Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }*/

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
        initializeView(fragment);
        addView();
        for(Map.Entry<Character, ImageView> entry : imageViewMap.entrySet()) {
            entry.getValue().setOnClickListener(unused -> imageClicked(entry.getKey()));
        }
        /*for (EditText eachView : textViewList) {
            eachView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }*/
        for (EditText eachView : textViewList) {
            eachView.setOnClickListener(unused -> viewClicked(eachView));
        }
        clear.setOnClickListener(unused -> clearInput());
        solve.setOnClickListener(unused -> solveClicked());


    }

    private void viewClicked(EditText view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String viewId = view.getResources().getResourceEntryName(view.getId());
        char whichSide = viewId.charAt(0);
        int whichPos = Integer.parseInt(viewId.substring(1));
        builder.setTitle("You are now setting the color for "+ viewId);
        builder.setMessage("Please enter ONE, LOWER-CASE letter: r as red, g as green, b as blue, w as white, o as orange, and y as yellow");
// Set up the input
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setHint("Enter your letter here: ");
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                char userInput = input.getText().toString().charAt(0);
                if (userInput == 'g' || userInput == 'G') {
                    view.setText("g");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'w' || userInput == 'W') {
                    view.setText("w");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'o' || userInput == 'O'){
                    view.setText("o");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'r' || userInput == 'R') {
                    view.setText("r");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'y' || userInput == 'Y') {
                    view.setText("y");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'b'  || userInput == 'B') {
                    view.setText("b");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else {
                    inValidEntry(view);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void addUserInputToArray(char whichSide,int whichPos, char userInput) {
        switch (whichSide) {
            case 'U':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        u[i] = toLowerCase(userInput);
                        System.out.println("u side at position " + whichPos + " is " +  u[i]);
                        break;
                    }
                }
                break;
            case 'L':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        l[i] = toLowerCase(userInput);
                        System.out.println("l side at position " + whichPos + " is " +  l[i]);
                        break;
                    }
                }
                break;
            case 'F':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        f[i] = toLowerCase(userInput);
                        System.out.println("f side at position " + whichPos + " is " +  f[i]);
                        break;
                    }
                }
                break;
            case 'R':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        r[i] = toLowerCase(userInput);
                        System.out.println("r side at position " + whichPos + " is " +  f[i]);
                        break;
                    }
                }
                break;
            case 'B':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        b[i] = toLowerCase(userInput);
                        System.out.println("b side at position " + whichPos + " is " +  b[i]);
                        break;
                    }
                }
                break;
            case 'D':
                for (int i = 0; i < 9; i ++) {
                    if (i == (whichPos - 1)) {
                        d[i] = toLowerCase(userInput);
                        System.out.println("d side at position " + whichPos + " is " +  d[i]);
                        break;
                    }
                }
                break;
        }
    }
    private void inValidEntry(EditText view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String viewId = view.getResources().getResourceEntryName(view.getId());
        char whichSide = viewId.charAt(0);
        int whichPos = Integer.parseInt(viewId.substring(1));
        builder.setTitle("NON-VALID entry for " + viewId + "! Please Retry. ");
        builder.setMessage("Please enter ONE, LOWER-CASE letter: r as red, g as green, b as blue, w as white, o as orange, and y as yellow");
// Set up the input
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setHint("Enter your letter here: ");
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                char userInput = input.getText().toString().charAt(0);
                if (userInput == 'g' || userInput == 'G') {
                    view.setText("g");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'w' || userInput == 'W') {
                    view.setText("w");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'o' || userInput == 'O'){
                    view.setText("o");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'r' || userInput == 'R') {
                    view.setText("r");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'y' || userInput == 'Y') {
                    view.setText("y");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else if (userInput == 'b'  || userInput == 'B') {
                    view.setText("b");
                    addUserInputToArray(whichSide, whichPos, userInput);
                } else {
                    inValidEntry(view);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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

        // Convert traditional RGB chars to to UBL definition used by min2phase*/
        initilizeCenterPiece();
        String rgbCube = new String(u) + new String(r) + new String(f) + new String(d) + new String(l) + new String(b);
        Log.d("cube before switching: ", rgbCube);
        rgbCube = rgbCube.replace(u[4], 'U');
        rgbCube = rgbCube.replace(r[4], 'R');
        rgbCube = rgbCube.replace(f[4], 'F');
        rgbCube = rgbCube.replace(d[4], 'D');
        rgbCube = rgbCube.replace(l[4], 'L');
        rgbCube = rgbCube.replace(b[4], 'B');
        Log.d("Cube after switching: ", rgbCube);


        //String rgbCube = "DUUBULDBFRBFRRULLLBRDFFFBLURDBFDFDRFRULBLUFDURRBLBDUDL";
        //String rgbCube = Tools.randomCube();
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("By clicking YES, you will restart the solver.");
        alertDialogBuilder
                .setMessage("Click NO to get back.")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Log.d("Clearing input clicked: ", "not doing anything yet");

    }

    /** Called when user tabs specific block to take picture of cube.
     *  Should take photo - read color - and update stored color in array.
     * @param p : a char that takes the position of user's tab.
     */
    private void imageClicked(Character p) {
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
                System.out.println("Arrived here");
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
        System.out.println("Arrived here at takePic");
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
            System.out.println(centerPosition * 9 + i);
            if (textViewList.get(centerPosition * 9 + i) != null) {
                textViewList.get(centerPosition * 9 + i).setText(new Pixel(Color.red(p), Color.green(p), Color.blue(p)).getColor());
                temp[i] = new Pixel(Color.red(p), Color.green(p), Color.blue(p)).getColor().charAt(0);
            }
        }
    }

    //Declaring views for showing the output result.

    EditText u1;
    EditText u2;

    EditText u3;
    //@BindView(R.id.U4)
    EditText u4;
    //@BindView(R.id.U5)
    EditText u5;
    //@BindView(R.id.U6)
    EditText u6;
    //@BindView(R.id.U7)
    EditText u7;
    //@BindView(R.id.U8)
    EditText u8;
    //@BindView(R.id.U9)
    EditText u9;

    //@BindView(R.id.F1)
    EditText f1;
    //@BindView(R.id.F2)
    EditText f2;
    //@BindView(R.id.F3)
    EditText f3;
    //@BindView(R.id.F4)
    EditText f4;
    //@BindView(R.id.F5)
    EditText f5;
    //@BindView(R.id.F6)
    EditText f6;
    //@BindView(R.id.F7)
    EditText f7;
    //@BindView(R.id.F8)
    EditText f8;
    //@BindView(R.id.F9)
    EditText f9;

    //@BindView(R.id.D1)
    EditText d1;
    //@BindView(R.id.D2)
    EditText d2;
    //@BindView(R.id.D3)
    EditText d3;
    //@BindView(R.id.D4)
    EditText d4;
    //@BindView(R.id.D5)
    EditText d5;
    //@BindView(R.id.D6)
    EditText d6;
    //@BindView(R.id.D7)
    EditText d7;
    //@BindView(R.id.D8)
    EditText d8;
    //@BindView(R.id.D9)
    EditText d9;

    //@BindView(R.id.L1)
    EditText l1;
    //@BindView(R.id.L2)
    EditText l2;
    //@BindView(R.id.L3)
    EditText l3;
    //@BindView(R.id.L4)
    EditText l4;
    //@BindView(R.id.L5)
    EditText l5;
    //@BindView(R.id.L6)
    EditText l6;
    //@BindView(R.id.L7)
    EditText l7;
    //@BindView(R.id.L8)
    EditText l8;
    //@BindView(R.id.L9)
    EditText l9;

    //@BindView(R.id.R1)
    EditText r1;
    //@BindView(R.id.R2)
    EditText r2;
    //@BindView(R.id.R3)
    EditText r3;
    //@BindView(R.id.R4)
    EditText r4;
    //@BindView(R.id.R5)
    EditText r5;
    //@BindView(R.id.R6)
    EditText r6;
    //@BindView(R.id.R7)
    EditText r7;
    //@BindView(R.id.R8)
    EditText r8;
    //@BindView(R.id.R9)
    EditText r9;

    //@BindView(R.id.B1)
    EditText b1;
    //@BindView(R.id.B2)
    EditText b2;
    //@BindView(R.id.B3)
    EditText b3;
    //@BindView(R.id.B4)
    EditText b4;
    //@BindView(R.id.B5)
    EditText b5;
    //@BindView(R.id.B6)
    EditText b6;
    //@BindView(R.id.B7)
    EditText b7;
    //@BindView(R.id.B8)
    EditText b8;
    //@BindView(R.id.B9)
    EditText b9;

    public void initializeView(Fragment fragment) {
        u1 = fragment.getView().findViewById(R.id.U1);
        u2 = fragment.getView().findViewById(R.id.U2);
        u3 = fragment.getView().findViewById(R.id.U3);
        u4 = fragment.getView().findViewById(R.id.U4);
        u5 = fragment.getView().findViewById(R.id.U5);
        u6 = fragment.getView().findViewById(R.id.U6);
        u7 = fragment.getView().findViewById(R.id.U7);
        u8 = fragment.getView().findViewById(R.id.U8);
        u9 = fragment.getView().findViewById(R.id.U9);

        f1 = fragment.getView().findViewById(R.id.F1);
        f2 = fragment.getView().findViewById(R.id.F2);
        f3 = fragment.getView().findViewById(R.id.F3);
        f4 = fragment.getView().findViewById(R.id.F4);
        f5 = fragment.getView().findViewById(R.id.F5);
        f6 = fragment.getView().findViewById(R.id.F6);
        f7 = fragment.getView().findViewById(R.id.F7);
        f8 = fragment.getView().findViewById(R.id.F8);
        f9 = fragment.getView().findViewById(R.id.F9);

        d1 = fragment.getView().findViewById(R.id.D1);
        d2 = fragment.getView().findViewById(R.id.D2);
        d3 = fragment.getView().findViewById(R.id.D3);
        d4 = fragment.getView().findViewById(R.id.D4);
        d5 = fragment.getView().findViewById(R.id.D5);
        d6 = fragment.getView().findViewById(R.id.D6);
        d7 = fragment.getView().findViewById(R.id.D7);
        d8 = fragment.getView().findViewById(R.id.D8);
        d9 = fragment.getView().findViewById(R.id.D9);

        l1 = fragment.getView().findViewById(R.id.L1);
        l2 = fragment.getView().findViewById(R.id.L2);
        l3 = fragment.getView().findViewById(R.id.L3);
        l4 = fragment.getView().findViewById(R.id.L4);
        l5 = fragment.getView().findViewById(R.id.L5);
        l6 = fragment.getView().findViewById(R.id.L6);
        l7 = fragment.getView().findViewById(R.id.L7);
        l8 = fragment.getView().findViewById(R.id.L8);
        l9 = fragment.getView().findViewById(R.id.L9);

        r1 = fragment.getView().findViewById(R.id.R1);
        r2 = fragment.getView().findViewById(R.id.R2);
        r3 = fragment.getView().findViewById(R.id.R3);
        r4 = fragment.getView().findViewById(R.id.R4);
        r5 = fragment.getView().findViewById(R.id.R5);
        r6 = fragment.getView().findViewById(R.id.R6);
        r7 = fragment.getView().findViewById(R.id.R7);
        r8 = fragment.getView().findViewById(R.id.R8);
        r9 = fragment.getView().findViewById(R.id.R9);

        b1 = fragment.getView().findViewById(R.id.B1);
        b2 = fragment.getView().findViewById(R.id.B2);
        b3 = fragment.getView().findViewById(R.id.B3);
        b4 = fragment.getView().findViewById(R.id.B4);
        b5 = fragment.getView().findViewById(R.id.B5);
        b6 = fragment.getView().findViewById(R.id.B6);
        b7 = fragment.getView().findViewById(R.id.B7);
        b8 = fragment.getView().findViewById(R.id.B8);
        b9 = fragment.getView().findViewById(R.id.B9);
    }
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

        textViewList.add(l1);
        textViewList.add(l2);
        textViewList.add(l3);
        textViewList.add(l4);
        textViewList.add(l5);
        textViewList.add(l6);
        textViewList.add(l7);
        textViewList.add(l8);
        textViewList.add(l9);

        textViewList.add(f1);
        textViewList.add(f2);
        textViewList.add(f3);
        textViewList.add(f4);
        textViewList.add(f5);
        textViewList.add(f6);
        textViewList.add(f7);
        textViewList.add(f8);
        textViewList.add(f9);

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

        textViewList.add(d1);
        textViewList.add(d2);
        textViewList.add(d3);
        textViewList.add(d4);
        textViewList.add(d5);
        textViewList.add(d6);
        textViewList.add(d7);
        textViewList.add(d8);
        textViewList.add(d9);
    }
    private void initilizeCenterPiece() {
        u[5] = 'w';
        l[5] = 'o';
        f[5] = 'g';
        r[5] = 'r';
        b[5] = 'b';
        d[5] = 'y';
    }
}
