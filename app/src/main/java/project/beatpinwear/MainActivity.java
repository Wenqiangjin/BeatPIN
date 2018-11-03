package project.beatpinwear;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridViewPager pager =  findViewById(R.id.pager);
        pager.setAdapter(new ViewAdapter(this, getFragmentManager()));

        final DotsPageIndicator dotsPageIndicator =  findViewById(R.id.pager_indicator);
        dotsPageIndicator.setPager(pager);

        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {
                dotsPageIndicator.onPageScrolled(i, i1, v, v1, i2, i3);
            }

            @Override
            public void onPageSelected(int i, int i1) {
                dotsPageIndicator.onPageSelected(i, i1);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                dotsPageIndicator.onPageScrollStateChanged(i);
            }
        });
    }


    public class ViewAdapter extends FragmentGridPagerAdapter {
        Context mContext;
        SetupFragment setupFragment;
        SetupFragment2 setupFragment2;
        DoneFragment doneFragment;
        TestFragment testFragment;

        public ViewAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getFragment(int row, int col) {

            if(col == 0) {
                setupFragment = new SetupFragment();
                return setupFragment;
            }
            else if(col == 1)  {
                setupFragment2 = new SetupFragment2();
                return setupFragment2;
            }
            else if(col == 2)   {
                doneFragment = new DoneFragment();
                return doneFragment;
            }
            else {
                testFragment = new TestFragment();
                return testFragment;
            }
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int i) {
            return 4;
        }
    }



}
