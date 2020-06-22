package com.example.ee3070t12.Fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.ee3070t12.Objects.Item;
import com.example.ee3070t12.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    public static ArrayList<Item> itemList = new ArrayList<Item>();
    public static ArrayList<Item> resultList = new ArrayList<Item>();
    SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_search, container, false);

        readItem();

        final SwipeMenuListView swipeMenuListView = (SwipeMenuListView)root.findViewById(R.id.itemList);

        if(getContext() == null){

            System.out.println("getContext is null");
        }


        final ArrayAdapter itemlistadapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_1, itemList);
        final ArrayAdapter resultlistadapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_1, resultList);
        swipeMenuListView.setAdapter(itemlistadapter);


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item

                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x00,
                        0x00)));
                openItem.setWidth(170);
                openItem.setIcon(R.drawable.ic_add_3);
                menu.addMenuItem(openItem);

                SwipeMenuItem path = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                path.setBackground(new ColorDrawable(Color.rgb(0xFF,
                        0x00, 0xFF)));
                path.setWidth(170);
                path.setIcon(R.drawable.ic_path);
                menu.addMenuItem(path);
            }
        };
        // set creator
        swipeMenuListView.setMenuCreator(creator);



        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // add
                        ShoplistFragment.shoppingList.add(resultList.get(position));
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Adding item to your shopping list",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case 1:
                        // navigation
                        try{
                            HomeFragment.providePath(HomeFragment.userCoor,
                                    resultList.get(position).getLocation(), HomeFragment.root_Home);
                            Toast toast_2 = Toast.makeText(getActivity().getApplicationContext(),
                                    "Finding path..",Toast.LENGTH_SHORT);
                            toast_2.show();
                        }
                        catch(Exception e){
                            Toast toast_3 = Toast.makeText(getActivity().getApplicationContext(),
                                    "Please find your location first",Toast.LENGTH_SHORT);
                            toast_3.show();

                            e.printStackTrace();
                        }

                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


        searchView = root.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                resultList.clear();
                String q = query.toLowerCase();
                for(Item i: itemList){
                    if(i.getName().toLowerCase().contains(q))
                        resultList.add(i);
                }

                swipeMenuListView.setAdapter(resultlistadapter);

                if(resultList.size() == 0){
                    Toast toast1 = Toast.makeText(getActivity().getApplicationContext(),
                            "Result not found",Toast.LENGTH_SHORT);
                    toast1.show();
                }

                return false;
            }



            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });



        return root;
    }




    public void readItem(){

        try{
            InputStream is = getActivity().getAssets().open("itemlist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null){

                String[] item = line.split(":");

                String name = item[0];
                double price = Double.parseDouble(item[1]);
                int location = Integer.parseInt(item[2]);

                Item item1 = new Item(name, price, location);
                itemList.add(item1);
                resultList.add(item1);
                System.out.println("adding " + item1.toString());

                line = reader.readLine();
            }
        }
        catch (IOException e){
            System.out.println("File not found");
            e.printStackTrace();
        }
    }
}
