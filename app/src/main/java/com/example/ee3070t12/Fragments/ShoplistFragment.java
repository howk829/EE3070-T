package com.example.ee3070t12.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.ee3070t12.util;

import java.util.ArrayList;

public class ShoplistFragment extends Fragment {

    public static ArrayList<Item> shoppingList = new ArrayList<>();
    public static int discountType;
    ArrayAdapter resultadapter;
    SwipeMenuListView swipeMenuListView;
    Button refreshButton;
    Button discountButton;
    TextView totalprice;
    String total = "Total: $";
    double price;

    boolean DiscountClicked = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_shoplist, container, false);



        swipeMenuListView = (SwipeMenuListView)root.findViewById(R.id.shopList);
        resultadapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_1, shoppingList);

        swipeMenuListView.setAdapter(resultadapter);


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item

                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x00,
                        0x00)));
                openItem.setWidth(170);
                openItem.setIcon(R.drawable.ic_delete);
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
                        // delete
                        shoppingList.remove(position);
                        swipeMenuListView.setAdapter(resultadapter);
                        updatePrice();
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Removing item from your shopping list",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case 1:
                        // navigation
                        HomeFragment.providePath(HomeFragment.userCoor,
                                shoppingList.get(position).getLocation(), HomeFragment.root_Home);
                        Toast toast_2 = Toast.makeText(getActivity().getApplicationContext(),
                                "Finding path..",Toast.LENGTH_SHORT);
                        toast_2.show();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });



        totalprice = root.findViewById(R.id.totalprice);

        refreshButton = root.findViewById(R.id.refreshButton);         //refresh button listener
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeMenuListView.setAdapter(resultadapter);
                updatePrice();
            }
        });


        discountButton = root.findViewById(R.id.discountButton);
        discountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

                if (!DiscountClicked)
                {
                    alertDialog.setTitle("You get an offer!");
                    discountType = 1;//(int) (Math.random() * 3) + 1;

                    if (discountType == 1) {
                        alertDialog.setMessage("Drink and chips get 20% off! ");
                        DiscountClicked = true;
                        discountType = 1;
                    } else if (discountType == 2) {
                        alertDialog.setMessage("Stationary get 20% off!");
                        DiscountClicked = true;
                        discountType = 2;
                    } else {
                        alertDialog.setMessage("Fresh food get 20% off!");
                        DiscountClicked = true;
                        discountType = 3;
                    }
                }
                else
                {
                    alertDialog.setTitle("");
                    alertDialog.setMessage("You already get an offer.");
                }

                alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "GOT IT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });


        for(Item i:shoppingList){
            price += i.getPrice();
        }

        util.round(price, 2);

        totalprice.setText(total + Double.toString(price));


        return root;
    }

    public void onDiscountClicked(View root){



    }

    public void updatePrice(){

        price = 0;

        for(Item i:shoppingList){
            if(discountType == i.getLocation()){
                price += i.getPrice()*0.8;
            }
            else{
                price += i.getPrice();
            }
        }
        price = util.round(price, 2);
        totalprice.setText(total + Double.toString(price));

    }


}
