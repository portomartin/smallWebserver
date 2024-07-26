package com.martinporto.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.martinporto.model.JdiActivityLog;
import com.martinporto.model.JdiDatabase;
import com.martinporto.smallwebserver.R;

import java.util.ArrayList;

public class StatsFragment extends Fragment {
	
	ArrayAdapter arrayAdapter;
	
	public StatsFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.stats_fragment, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		
		TextView txtStats = view.findViewById(R.id.txtStats);
//		Integer total = JdiDatabase.get().getStats();
//		txtStats.setText("Total hits:" + total);
		
		/* listView */
		ListView listViewActivity;
		listViewActivity = view.findViewById(R.id.listLogs);
		arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.log_adapter, R.id.txtFile, JdiActivityLog.get().getLogs());
		listViewActivity.setAdapter(arrayAdapter);
		
		/* logs */
		JdiActivityLog.get().getmLogs().observe(getActivity(), new Observer<ArrayList<String>>() {
			@Override
			public void onChanged(ArrayList<String> strings) {
				arrayAdapter.notifyDataSetChanged();
				
/*				Integer total = JdiDatabase.get().getStats();
				txtStats.setText("Total hits:" + total);*/
			}
		});
	}
}
