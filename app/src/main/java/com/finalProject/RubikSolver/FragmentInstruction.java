package com.finalProject.RubikSolver;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentInstruction extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_instruction, container, false);
        TextView gitHubLink = toReturn.findViewById(R.id.gitHubLink);
        Log.d("Test link", gitHubLink.toString());
        gitHubLink.setMovementMethod(LinkMovementMethod.getInstance());
        TextView improve = toReturn.findViewById(R.id.improve);
        improve.setMovementMethod(LinkMovementMethod.getInstance());
        return toReturn;
    }
}
