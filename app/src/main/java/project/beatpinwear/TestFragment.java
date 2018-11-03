package project.beatpinwear;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment {

    private ImageButton test;

    private ImageButton check;

    private final String LOG = "Check";
    private int beatPinNum, beat_count;
    private static long touchDown_time, touchUp_time;
    private long prevUp = 0, prevDown = 0;

    private static ArrayList<Float> actionDowns;
    private static ArrayList<Float> actionsUps;
    private static ArrayList<Float> tapIntervals;
    boolean success = true;

    private DatabaseHandler database;



    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_test, container, false);

        test = view.findViewById(R.id.test);
        check = view.findViewById(R.id.check);
        database = new DatabaseHandler(getActivity().getApplicationContext());

        actionDowns = new ArrayList<Float>();
        actionsUps = new ArrayList<Float>();
        tapIntervals = new ArrayList<Float>();

        beat_count = 0;

        test.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())   {
                    case MotionEvent.ACTION_DOWN:
                        touchDown_time = event.getDownTime();
                        if(beat_count > 0) {
                            prevDown = prevUp + touchDown_time - touchUp_time;
    //                        Log.d(LOG, "Touch Down time : "+(touchDown_time-touchUp_time+prevUp));
                            actionDowns.add((float) prevDown);
                            tapIntervals.add(actionDowns.get(beat_count)-actionDowns.get(beat_count-1));
                        }

                        return true;
                    case MotionEvent.ACTION_UP:
                        touchUp_time = event.getEventTime();
                        if(beat_count == 0) {
    //                        Log.d(LOG, "Touch Down time : 0"+" - Touch Up Time:"+(touchUp_time-touchDown_time));
                            actionDowns.add((float) 0);
                            prevUp = touchUp_time - touchDown_time;
                            actionsUps.add((float) prevUp);
                        }
                        else    {
                            prevUp = prevDown + touchUp_time - touchDown_time;
    //                        Log.d(LOG, "Touch Up Time: "+(touchUp_time-touchDown_time+prevDown));
                            actionsUps.add((float) prevUp);
                        }
                        beat_count += 1;
                        return true;
                }

                return true;
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                success = true;
                if (beat_count != database.getFirstBeatPinLength(1)){
                    Toast.makeText(getActivity().getApplicationContext(), "Invalid user.", Toast.LENGTH_LONG).show();
                }
                    //Toast.makeText(getActivity().getApplicationContext(), "Number of taps doesn't match.", Toast.LENGTH_LONG).show();
                else {
                    Float[] avgs = database.getAvgsResults(beat_count);
                    Float[] sds = database.getSdResults(beat_count);

                    ArrayList<Float> tapIntervals2 = normalize(tapIntervals);
                    for(float value: tapIntervals2)
    //                    Log.d("Check", "User Tap Interval: "+value);

                    for(int i=0; i<beat_count-1; i++) {
                        if(tapIntervals2.get(i)-avgs[i]  <= 1 * sds[i]){}
    //                        Log.d(LOG, "Success for tap "+(i+1));
                        else    {
    //                        Log.d(LOG, "Fail for tap "+(i+1));
                            success = false;
                        }
                    }
                    float total_time = 0;
                    for(int i=0; i<actionsUps.size();i++)   {
                        total_time += actionsUps.get(i);
                    }
                    Log.d(LOG,"Total time: "+total_time);
                    if(success == true)
                        Toast.makeText(getActivity().getApplicationContext(), "Valid user.", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity().getApplicationContext(), "Invalid user.", Toast.LENGTH_LONG).show();
                }


                beat_count = 0;
                actionDowns.clear();
                actionsUps.clear();
                tapIntervals.clear();
            }
        });


        return view;
    }


    private ArrayList normalize(ArrayList<Float> tapIntervals)    {
        float max = tapIntervals.get(0);
        for(int i=1; i<tapIntervals.size(); i++)    {
            if(tapIntervals.get(i) > max)
                max = tapIntervals.get(i);
        }

        for(int i=0; i<tapIntervals.size(); i++)    {
            tapIntervals.set(i, tapIntervals.get(i)/max);
    //        Log.d(LOG, "Tap interval after normalize: "+(i+1)+" - "+tapIntervals.get(i));
        }
        return tapIntervals;
    }

}
