package org.actionpath.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;


import org.actionpath.R;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDisplayExternalURLListener} interface
 * to handle interaction events.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment implements View.OnClickListener {
    private static String TAG = AboutFragment.class.getName();

    private OnDisplayExternalURLListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using any provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Loading the About Page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ImageView civicMediaLogo = (ImageView) view.findViewById(R.id.about_image_civicmedia_logo);
        civicMediaLogo.setOnClickListener(this);
        ImageView mediaLabLogo = (ImageView) view.findViewById(R.id.about_image_medialab_logo);
        mediaLabLogo.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_image_civicmedia_logo:
                visitUri("https://civic.mit.edu/");
                break;
            case R.id.about_image_medialab_logo:
                visitUri("https://media.mit.edu/");
                break;
        }
    }

    private void visitUri(String uri) {
        if (mListener != null) {
            mListener.onDisplayExternalURL(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDisplayExternalURLListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDisplayExternalURLListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDisplayExternalURLListener {
        void onDisplayExternalURL(String uri);
    }

}
