package com.finalProject.RubikSolver;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.finalProject.RubikSolver.ui.main.SectionsPagerAdapter;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    /** Char arrays that stores the color of each face.*/
    private char[] u = new char[9];
    private char[] f = new char[9];
    private char[] d = new char[9];
    private char[] l = new char[9];
    private char[] r = new char[9];
    private char[] b = new char[9];

    /** Solving fragment that handles all solving actions. */
    private Fragment solveTab;

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
     * @param fragment
     */
    private void handleSolvingAction(Fragment fragment) {
        // Left over testing code
        //TextView a = fragment.getView().findViewById(R.id.solveTest);
        //Log.d("textbox is ", a.getText().toString());

        Button solve = fragment.getView().findViewById(R.id.solve);
        Button clear = fragment.getView().findViewById(R.id.clear);

        clear.setOnClickListener(unused -> clearInput());
        solve.setOnClickListener(unused -> solveClicked());
    }

    /**
     * This function is called when "solving is clicked"
     * Pops out an alert dialog that shows the result.
     */
    private void solveClicked() {
        Log.d("clicking solved ", "Solving");

        // Inflate view for later use.
        View solutionDialog = getLayoutInflater()
                .inflate(R.layout.chunk_solution, null, false);
        TextView solution = solutionDialog.findViewById(R.id.algorithm);

        //This solution is set for testing.
        solution.setText("F D F' U R' L' F D' U R D' F' D2 R U2");

        //Pop an alert Dialog that shows solution.
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(solutionDialog)
                .setPositiveButton(R.string.seeAnimation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Open a webpage if user want to see animation.
                        // Currently using web browser. !TODO We could update it with an curl maybe?
                        Uri webpage = Uri.parse("https://alg.cubing.net/?setup=U2_R-_D2_F_D_R-_U-_D_F-_L_R_U-_F_D-_F-%0A&alg=F_D_F-_U_R-_L-_F_D-_U_R_D-_F-_D2_R_U2");
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
}