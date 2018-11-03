package project.beatpinwear;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class DoneFragment extends Fragment {

    Button done;

    private DatabaseHandler myDB;

    public DoneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_done, container, false);

        done = view.findViewById(R.id.done);
        myDB = new DatabaseHandler(getActivity().getApplicationContext());

        if(myDB.getTotalBeatPinNumber()  == 0)
            Toast.makeText(getActivity().getApplicationContext(), "Enter New Password.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity().getApplicationContext(), "Long press cancel to enter new password.", Toast.LENGTH_SHORT).show();


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        myDB.deleteFinalTableRows();
                        Float[] tappingIntervalAvgs =  myDB.getTappingIntervalAvgs( myDB.getFirstBeatPinLength(1));
                        Float[] tappingIntervalSds = myDB.getTappingIntervalsSDs( myDB.getFirstBeatPinLength(1));
                        myDB.insertResults(tappingIntervalAvgs, tappingIntervalSds, myDB.getFirstBeatPinLength(1));
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

}
