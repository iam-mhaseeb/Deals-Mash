package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ToysF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public ToysF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_toys, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new Toys(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }
}
