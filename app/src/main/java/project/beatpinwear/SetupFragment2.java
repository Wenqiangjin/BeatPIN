package project.beatpinwear;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetupFragment2 extends Fragment {

    ImageButton save, cancel;
    Button tap;
    TextView countView;

    private static long touchDown_time, touchUp_time;
    private final String LOG = "Check";
    private int beatPinNum, beat_count;
    private long prevUp = 0, prevDown = 0;
    private DatabaseHandler myDB;

    private static ArrayList<Float> actionDowns;
    private static ArrayList<Float> actionsUps;
    private static ArrayList<Float> tapIntervals;

    Bundle bundle;

    private boolean doneClicked = false;

    public SetupFragment2() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_setup_fragment2, container, false);

        bundle = new Bundle();

        tap =  view.findViewById(R.id.tap);
        save = view.findViewById(R.id.save);
        cancel = view.findViewById(R.id.cancel);
        countView = view.findViewById(R.id.count);

        myDB = new DatabaseHandler(getActivity().getApplicationContext());

        actionDowns = new ArrayList<Float>();
        actionsUps = new ArrayList<Float>();
        tapIntervals = new ArrayList<Float>();

        beat_count = 0;
        beatPinNum = 1;

        tap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(doneClicked == false)    {
                    switch(event.getAction())   {
                        case MotionEvent.ACTION_DOWN:
                            touchDown_time = event.getDownTime();
                            if(beat_count > 0) {
                                prevDown = prevUp + touchDown_time - touchUp_time;
    //                            Log.d(LOG, "Touch Down time : "+(touchDown_time-touchUp_time+prevUp));
                                actionDowns.add((float) prevDown);
                                tapIntervals.add(actionDowns.get(beat_count)-actionDowns.get(beat_count-1));
                            }

                            return true;
                        case MotionEvent.ACTION_UP:
                            touchUp_time = event.getEventTime();
                            if(beat_count == 0) {
     //                           Log.d(LOG, "Touch Down time : 0"+" - Touch Up Time:"+(touchUp_time-touchDown_time));
                                actionDowns.add((float) 0);
                                prevUp = touchUp_time - touchDown_time;
                                actionsUps.add((float) prevUp);
                            }
                            else    {
                                prevUp = prevDown + touchUp_time - touchDown_time;
    //                            Log.d(LOG, "Touch Up Time: "+(touchUp_time-touchDown_time+prevDown));
                                actionsUps.add((float) prevUp);
                            }
                            beat_count += 1;
                            return true;
                    }
                }
                else    {
                    Toast.makeText(getActivity().getApplicationContext(), "Already Password saved. Click delete and start again.", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tapIntervals.size() != 0) {
                    if(beatPinNum == 1)  {
                        if(myDB.tableHasRows())   {
                            if(countView.getText().equals(String.valueOf(0)))  {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Password already exists. Delete first by long pressing cancel.", Toast.LENGTH_SHORT).show();
                            }
                        }   else    {
                            int count =  myDB.insert(beatPinNum, actionDowns.size(), actionDowns, actionsUps);
    //                        Log.d(LOG, "--Beats inserted into the database: "+count);
                            for(float value: tapIntervals)
    //                            Log.d(LOG, "--TapIntervals: "+value);

                            tapIntervals = normalize(tapIntervals);
                            //                    Log.d(LOG, "Tap Intervals size: "+tapIntervals.size());
                            //                    for(float value: tapIntervals)
                            //                        Log.d(LOG, "Tap Intervals : "+value);

                            int count1 = myDB.insertTapIntervals(beatPinNum, tapIntervals);
    //                        Log.d(LOG, "--TapIntervals count: "+count1);

                            //            myDB.getAllRows(beatPinNum);
                            //            Toast.makeText(getActivity().getApplicationContext(), "Count : "+beatPinNum, Toast.LENGTH_LONG).show();
                            countView.setText(String.valueOf(beatPinNum));
                            bundle.putInt("beatPinNum",beatPinNum);
                            float total_time = 0;
                            for(int i=0; i<actionsUps.size();i++)   {
                                total_time += actionsUps.get(i);
                            }
                            Log.d(LOG,"Total time: "+total_time);
                            }
                        }
                    if(beatPinNum > 1)  {
                        if(myDB.getFirstBeatPinLength(1) != beat_count)  {
                            int count1 = myDB.getFirstBeatPinLength(1);
                            Toast.makeText(getActivity().getApplicationContext(), "Beatpin doesn't match to the previous.", Toast.LENGTH_LONG).show();
                            beatPinNum = beatPinNum - 1;
                        }
                        else    {
                            int count = myDB.insert(beatPinNum, actionDowns.size(), actionDowns, actionsUps);
    //                        Log.d(LOG, "--Beats inserted into the database: "+count);
                            for(float value: tapIntervals)
    //                            Log.d(LOG, "--TapIntervals: "+value);

                            tapIntervals = normalize(tapIntervals);

                            int count1 = myDB.insertTapIntervals(beatPinNum, tapIntervals);
    //                        Log.d(LOG, "--TapIntervals count: "+count1);
                            //                myDB.getAllRows(beatPinNum);
        //                    Toast.makeText(getActivity().getApplicationContext(), "Count : "+beatPinNum, Toast.LENGTH_LONG).show();
                            countView.setText(String.valueOf(beatPinNum));
                            bundle.putInt("beatPinNum",beatPinNum);
                            float total_time = 0;
                            for(int i=0; i<actionsUps.size();i++)   {
                                total_time += actionsUps.get(i);
                            }
                            Log.d(LOG,"Total time: "+total_time);
                        }
                    }
                    beatPinNum += 1;
                    beat_count = 0;
                    actionDowns.clear();
                    actionsUps.clear();
                    tapIntervals.clear();
                }   else    {
                    Toast.makeText(getActivity().getApplicationContext(), "Enter taps.", Toast.LENGTH_LONG).show();
                }
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beat_count = 0;
                actionDowns.clear();
                actionsUps.clear();
                tapIntervals.clear();
            }
        });

        cancel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myDB.deleteAll();
                beat_count = 0;
                beatPinNum = 1;
                actionDowns.clear();
                actionsUps.clear();
                tapIntervals.clear();
                countView.setText(String.valueOf(0));
                Toast.makeText(getActivity().getApplicationContext(), "All prev password deleted.",Toast.LENGTH_LONG).show();
                return true;
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
