package uc3mprojects.pablo.ex1aliamate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Pablo Alías Mateos on 02/10/2016.
 */

public class fragment_survey extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.fragment_survey, container);     // to inflate fragment xml code

        // Button b = view.findViewById(); // to access fragments views, it is needed to use view previous object

        return view;
    }

}
