package com.datenkrake.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.datenkrake.R;

/**
 * TabbedFragment. Container-Fragment with Tabs for showing multiple Child-Fragments.
 * 
 * @author svenja
 */
public class TabbedFragment extends Fragment {
	private Context context;
	
	static final String ARG_ITEM_NUMBER = "item_nr";
	private int itemNr;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private PagerTabStrip titleStrip;

	/**
	 * Returns a new instance of this fragment.
	 */
	public static TabbedFragment newInstance(Context context, int itemNr) {
		TabbedFragment fragment = new TabbedFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_ITEM_NUMBER, itemNr);
		fragment.setArguments(args);
		return fragment;
	}

	public TabbedFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    context = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			itemNr = getArguments().getInt(ARG_ITEM_NUMBER);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
		int tabNr = context.getResources().getIntArray(R.array.tab_nrs)[itemNr];
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(), tabNr);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        titleStrip = (PagerTabStrip) rootView.findViewById(R.id.fragment_tabbed_tab_strip);
        titleStrip.setTabIndicatorColor(getResources().getColor(R.color.tab_indicator_color));
        titleStrip.setDrawFullUnderline(true);
        if (tabNr < 2){
        	titleStrip.setVisibility(View.GONE);
        }
        mViewPager.setAdapter(mSectionsPagerAdapter);
		return rootView;
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		int tabNr;

		public SectionsPagerAdapter(FragmentManager fm, int tabNr) {
			super(fm);
			this.tabNr = tabNr;
		}

		@Override
		public Fragment getItem(int sectionNr) {
			switch (itemNr) {
			case (0):
				return OverviewFragment.newInstance();
			case (1):
				return ContactsFragment.newInstance();
			case (2):
				switch (sectionNr) {
				case (0):
					return LearnAreasFragment.newInstance();
				case (1):
					return RecentCellsFragment.newInstance();
				}
			case (3):
				switch (sectionNr) {
				case (0):
					return PhoneTimeFragment.newInstance();
				case (1):
					return AppTimesFragment.newInstance();
				}
			case(4):
				switch (sectionNr) {
				case (0):
					return CommunicationFragment.newInstance();
				}
			case(5):
				switch (sectionNr) {
				case (0):
					return PerceivedStressFragment.newInstance();
				case (1):
					return MeasuredStressFragment.newInstance();
				}
			}
			return new Fragment();
		}

		@Override
		public int getCount() {
			return tabNr;
		}

		@Override
		public CharSequence getPageTitle(int sectionNr) {
			switch (itemNr) {
			case (0):
				return context.getResources().getString(R.string.title_fragment_overview);
			case (1):
				return context.getResources().getString(R.string.title_fragment_important_contacts);
			case (2):
				switch (sectionNr) {
				case (0):
					return context.getResources().getString(R.string.title_fragment_learn_areas);
				case (1):
					return context.getResources().getString(R.string.title_fragment_recent_cells);
				}
			case (3):
				switch (sectionNr) {
				case (0):
					return context.getResources().getString(R.string.title_fragment_phone_time);
				case (1):
					return context.getResources().getString(R.string.title_fragment_app_times);
				}
			case(4):
				switch (sectionNr) {
				case (0):
					return context.getResources().getString(R.string.title_fragment_communication_statistics);
				}
			case(5):
				switch (sectionNr) {
				case (0):
					return context.getResources().getString(R.string.title_fragment_perceived_stress);
				case (1):
					return context.getResources().getString(R.string.title_fragment_measured_stress);
				}
			default:
				return "Tab";
			}
		}
	}
	
}
