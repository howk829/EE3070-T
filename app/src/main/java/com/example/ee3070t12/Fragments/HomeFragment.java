package com.example.ee3070t12.Fragments;
import  com.example.ee3070t12.*;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ee3070t12.Objects.Beacon;
import com.example.ee3070t12.Objects.DistBetween;
import com.example.ee3070t12.Objects.Node;
import com.example.ee3070t12.Objects.Path;
import com.example.ee3070t12.R;
import com.example.ee3070t12.geometry.Circle;
import com.example.ee3070t12.geometry.CircleCircleIntersection;
import com.example.ee3070t12.geometry.IntersectionCoor;
import com.example.ee3070t12.geometry.Vector2;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.ee3070t12.util.calculateDistance;
import static com.example.ee3070t12.util.calculateDistance2;

public class HomeFragment extends Fragment {

    public static ArrayList<Node> nodeList = new ArrayList<>();
    public static ArrayList<Path> pathList = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter;
    Button getlocButton;

    private final static int REQUEST_ENABLE_BT = 1;
    int PERMISSION_REQUEST_CODE = 1;


    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> detectedDevices = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();

    public static int[]  userCoor = new int[2];


    public static View root_Home;


    DocumentReference documentReference;


    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            Log.i("Action", action); // for debugging


            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE));
                String detected = name + ":" + rssi;
                Log.i ("Device Found","Name: "+ name + " Address: " + address + " RSSI: " + rssi); // for debugging
                if(!addresses.contains(address)){
                    addresses.add(address);
                    String deviceString = "";
                    if(name == null || name.equals("")){        //mac address
                        deviceString = address+"  RSSI: " + rssi + " dBm";
                    }
                    else{
                        deviceString = name + "  RSSI: " + rssi + " dBm";
                    }
                    bluetoothDevices.add(deviceString);
                    detectedDevices.add(detected);
                    //arrayAdapter.notifyDataSetChanged();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                plotDots(calCurrentloc(detectedDevices));

                documentReference = MainActivity.fStore
                        .collection("user_locations").document(MainActivity.userID);

                Map<String, Object> userdata = new HashMap<>();
                String curDate = DateFormat.getDateInstance().format(MainActivity.calendar.getTime());
                userdata.put("Date", curDate);
                userdata.put("coor_x", userCoor[0]);
                userdata.put("coor_y", userCoor[1]);

                documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(),"Success data saving",Toast.LENGTH_SHORT).show();
                    }
                });

                //statusTextView.setText("Finished");
                getlocButton.setEnabled(true);
            }
        }
    };

    public int[] calCurrentloc(ArrayList<String> detectedDevices){

        // 1 pixel = 0.01m   1m = 100 pixel
        final double n = 2.69*10;
        final double A = 61.57;
        int[] coordinate = new int[2];
        double[] beaconDist= {0,0,0}; // in pixel
        ArrayList<String> beaconId = new ArrayList<>();
        ArrayList<Beacon> beaconList = new ArrayList<>();
        ArrayList<Double[]> resultList = new ArrayList<>();

        Vector2 b1Coor = new Vector2(350,670);
        Vector2 b2Coor = new Vector2(650,470);
        Vector2 b3Coor = new Vector2(350,300);


        for(String s:detectedDevices){
            if(s.contains("Beacon"))
                beaconId.add(s);
        }

        if(beaconId.size() >= 3){
            for(String s1: beaconId){              //Create a list of Beacon object
                String[] parts = s1.split(":");
                String beaconName = parts[0];
                int rssiValue = Integer.parseInt(parts[1]);
                Beacon b = new Beacon(beaconName,rssiValue);
                beaconList.add(b);
            }



            for(Beacon b: beaconList){           //calculating corresponding distances in pixel
                if(b.name.equals("Beacon1")){
                    beaconDist[0] = Math.pow(10,(b.rssi - A)/n) * 100;
                }
                if(b.name.equals("Beacon2")){
                    beaconDist[1] = Math.pow(10,(b.rssi - A)/n) * 100;
                }
                if(b.name.equals("Beacon3")){
                    beaconDist[2] = Math.pow(10,(b.rssi - A)/n) * 100;
                }
            }

            Circle circle1 = new Circle(b1Coor,beaconDist[0]);
            Circle circle2 = new Circle(b2Coor,beaconDist[1]);
            Circle circle3 = new Circle(b3Coor,beaconDist[2]);

            CircleCircleIntersection intersection12 = new CircleCircleIntersection(circle1,circle2);
            CircleCircleIntersection intersection13 = new CircleCircleIntersection(circle1,circle3);
            CircleCircleIntersection intersection23 = new CircleCircleIntersection(circle2,circle3);

            double[] result12_1 = new double[2];     //all intersection point
            double[] result12_2 = new double[2];
            double[] result13_1 = new double[2];
            double[] result13_2 = new double[2];
            double[] result23_1 = new double[2];
            double[] result23_2 = new double[2];

            Vector2[] intersectionPoint12 = intersection12.getIntersectionPoints();
            Vector2[] intersectionPoint13 = intersection13.getIntersectionPoints();
            Vector2[] intersectionPoint23 = intersection23.getIntersectionPoints();

            try{

                if(intersection12.type.getIntersectionPointCount() == 2
                        && intersection13.type.getIntersectionPointCount() == 2
                        && intersection23.type.getIntersectionPointCount() == 2)
                {
                    System.out.println("intersect: 12, 13, 23");
                    result12_1[0] = intersectionPoint12[0].x;
                    result12_1[1] = intersectionPoint12[0].y;

                    result12_2[0] = intersectionPoint12[1].x;
                    result12_2[1] = intersectionPoint12[1].y;

                    result13_1[0] = intersectionPoint13[0].x;
                    result13_1[1] = intersectionPoint13[0].y;

                    result13_2[0] = intersectionPoint13[1].x;
                    result13_2[1] = intersectionPoint13[1].y;

                    result23_1[0] = intersectionPoint23[0].x;
                    result23_1[1] = intersectionPoint23[0].y;

                    result23_2[0] = intersectionPoint23[1].x;
                    result23_2[1] = intersectionPoint23[1].y;

                    ArrayList<IntersectionCoor> intersectionCoorList = new ArrayList();
                    ArrayList<IntersectionCoor> removeList = new ArrayList();

                    IntersectionCoor intersectioncoor1 = new IntersectionCoor(result12_1, 1);
                    IntersectionCoor intersectioncoor2 = new IntersectionCoor(result12_2, 2);
                    IntersectionCoor intersectioncoor3 = new IntersectionCoor(result13_1, 3);
                    IntersectionCoor intersectioncoor4 = new IntersectionCoor(result13_2, 4);
                    IntersectionCoor intersectioncoor5 = new IntersectionCoor(result23_1, 5);
                    IntersectionCoor intersectioncoor6 = new IntersectionCoor(result23_2, 6);

                    intersectionCoorList.add(intersectioncoor1);
                    intersectionCoorList.add(intersectioncoor2);
                    intersectionCoorList.add(intersectioncoor3);
                    intersectionCoorList.add(intersectioncoor4);
                    intersectionCoorList.add(intersectioncoor5);
                    intersectionCoorList.add(intersectioncoor6);

                    //find most left x coordinate (find min)
                    double minx = intersectioncoor1.getCoor1()[0];
                    for(IntersectionCoor i:intersectionCoorList){
                        if(i.getCoor1()[0] < minx)
                            minx = i.getCoor1()[0];
                    }
                    for(IntersectionCoor i2:intersectionCoorList){
                        if(i2.getCoor1()[0] == minx)
                            removeList.add(i2);
                    }

                    //find most right x coordinate (find max)
                    double maxx = intersectioncoor1.getCoor1()[0];
                    for(IntersectionCoor i3:intersectionCoorList){
                        if(i3.getCoor1()[0] > maxx)
                            maxx = i3.getCoor1()[0];
                    }
                    for(IntersectionCoor i4:intersectionCoorList){
                        if(i4.getCoor1()[0] == maxx){
                            removeList.add(i4);
                            break;
                        }
                    }

//                //find most top y coordinate (find min)
                    double miny = intersectioncoor1.getCoor1()[1];
                    for(IntersectionCoor i5:intersectionCoorList){
                        if(i5.getCoor1()[1] < miny)
                            miny = i5.getCoor1()[1];
                    }
                    for(IntersectionCoor i6:intersectionCoorList){
                        if(i6.getCoor1()[1] == miny){
                            removeList.add(i6);
                            break;
                        }
                    }
//                //find most buttom y coordinate (find max)
                    double maxy = intersectioncoor1.getCoor1()[1];
                    for(IntersectionCoor i7:intersectionCoorList){
                        if(i7.getCoor1()[1] > maxy)
                            maxy = i7.getCoor1()[0];
                    }
                    for(IntersectionCoor i8:intersectionCoorList){
                        if(i8.getCoor1()[1] == maxy){
                            removeList.add(i8);
                            break;
                        }
                    }

                    Collections.sort(removeList);

                    ArrayList removeList2 = new ArrayList<>();

                    for(IntersectionCoor i: removeList){
                        removeList2.add(i.getPointID());
                    }

                    for(IntersectionCoor i9: removeList){
                        System.out.println(i9.getPointID());
                    }


                    ArrayList <IntersectionCoor>finalList = new ArrayList<>();
                    for(IntersectionCoor i : intersectionCoorList){
                        if(!removeList2.contains(i.getPointID()))
                            finalList.add(i);
                    }


                    for(IntersectionCoor i9: finalList){
                        System.out.println(i9.getPointID());
                    }

                    for(IntersectionCoor i: finalList){
                        coordinate[0] += i.getCoor1()[0] / 3;
                        coordinate[1] += i.getCoor1()[1] / 3;
                    }
                    System.out.println(coordinate[0]);
                    System.out.println(coordinate[1]);

                    System.out.println(Arrays.toString(result12_1));
                    System.out.println(Arrays.toString(result12_2));
                    System.out.println(Arrays.toString(result13_1));
                    System.out.println(Arrays.toString(result13_2));
                    System.out.println(Arrays.toString(result23_1));
                    System.out.println(Arrays.toString(result23_2));
                }
                else if(intersection12.type.getIntersectionPointCount() == 2 &&
                        intersection13.type.getIntersectionPointCount() == 2 )
                {
                    System.out.println("intersect: 12, 13");
                    result12_1[0] = intersectionPoint12[0].x;
                    result12_1[1] = intersectionPoint12[0].y;

                    result12_2[0] = intersectionPoint12[1].x;
                    result12_2[1] = intersectionPoint12[1].y;

                    result13_1[0] = intersectionPoint13[0].x;
                    result13_1[1] = intersectionPoint13[0].y;

                    result13_2[0] = intersectionPoint13[1].x;
                    result13_2[1] = intersectionPoint13[1].y;


                    ArrayList<DistBetween> DistList = new ArrayList<>();


                    DistBetween dist1 = new DistBetween(calculateDistance(result12_1,result12_2),result12_1,result12_2);
                    DistBetween dist2 = new DistBetween(calculateDistance(result12_1,result13_1),result12_1,result13_1);
                    DistBetween dist3 = new DistBetween(calculateDistance(result12_1,result13_2),result12_1,result13_2);
                    DistBetween dist4 = new DistBetween(calculateDistance(result12_2,result13_1),result12_2,result13_1);
                    DistBetween dist5 = new DistBetween(calculateDistance(result12_2,result13_1),result12_2,result23_1);
                    DistBetween dist6 = new DistBetween(calculateDistance(result13_1,result13_2),result13_1,result13_2);
                    DistList.add(dist1);
                    DistList.add(dist2);
                    DistList.add(dist3);
                    DistList.add(dist6);
                    DistList.add(dist5);
                    DistList.add(dist4);

                    double min_dist = dist1.getDistance();
                    for(DistBetween db: DistList){
                        if(db.getDistance()< min_dist){
                            min_dist = db.getDistance();
                        }
                    }

                    for(DistBetween db1: DistList){
                        if(db1.getDistance() == min_dist){
                            coordinate[0] = (int) (db1.getFirst_coor()[0] + db1.getSecond_coor()[0])/2;
                            coordinate[1] = (int) (db1.getFirst_coor()[1] + db1.getSecond_coor()[1])/2;
                        }

                    }
                    System.out.println(Arrays.toString(result12_1));
                    System.out.println(Arrays.toString(result12_2));
                    System.out.println(Arrays.toString(result13_1));
                    System.out.println(Arrays.toString(result13_2));
                }
                else if (intersection13.type.getIntersectionPointCount() == 2 &&
                        intersection23.type.getIntersectionPointCount() == 2)
                {
                    System.out.println("intersect: 13, 23");
                    result13_1[0] = intersectionPoint13[0].x;
                    result13_1[1] = intersectionPoint13[0].y;

                    result13_2[0] = intersectionPoint13[1].x;
                    result13_2[1] = intersectionPoint13[1].y;

                    result23_1[0] = intersectionPoint23[0].x;
                    result23_1[1] = intersectionPoint23[0].y;

                    result23_2[0] = intersectionPoint23[1].x;
                    result23_2[1] = intersectionPoint23[1].y;

                    //todo: get the minimum distance between any 3 coordinate

                    ArrayList<DistBetween> DistList = new ArrayList<>();

                    DistBetween dist1 = new DistBetween(calculateDistance(result13_1,result13_2),result13_1,result13_2);
                    DistBetween dist2 = new DistBetween(calculateDistance(result13_1,result23_1),result13_1,result23_1);
                    DistBetween dist3 = new DistBetween(calculateDistance(result13_1,result23_2),result13_1,result23_2);
                    DistBetween dist4 = new DistBetween(calculateDistance(result13_2,result23_1),result13_2,result23_1);
                    DistBetween dist5 = new DistBetween(calculateDistance(result13_2,result23_2),result13_2,result23_2);
                    DistBetween dist6 = new DistBetween(calculateDistance(result23_1,result23_2),result23_1,result23_2);
                    DistList.add(dist1);
                    DistList.add(dist2);
                    DistList.add(dist3);
                    DistList.add(dist6);
                    DistList.add(dist5);
                    DistList.add(dist4);

                    double min_dist = dist1.getDistance();
                    for(DistBetween db: DistList){
                        if( db.getDistance() < min_dist ){
                            min_dist = db.getDistance();
                        }
                    }

                    for(DistBetween db1: DistList){
                        if(db1.getDistance() == min_dist){
                            coordinate[0] = (int) (db1.getFirst_coor()[0] + db1.getSecond_coor()[0])/2;
                            coordinate[1] = (int) (db1.getFirst_coor()[1] + db1.getSecond_coor()[1])/2;
                        }

                    }

                    System.out.println(Arrays.toString(result13_1));
                    System.out.println(Arrays.toString(result13_2));
                    System.out.println(Arrays.toString(result23_1));
                    System.out.println(Arrays.toString(result23_2));


                }
                else if (intersection12.type.getIntersectionPointCount() == 2 &&
                        intersection23.type.getIntersectionPointCount() == 2)
                {
                    System.out.println("intersect: 12, 23");

                    result12_1[0] = intersectionPoint12[0].x;
                    result12_1[1] = intersectionPoint12[0].y;

                    result12_2[0] = intersectionPoint12[1].x;
                    result12_2[1] = intersectionPoint12[1].y;

                    result23_1[0] = intersectionPoint23[0].x;
                    result23_1[1] = intersectionPoint23[0].y;

                    result23_2[0] = intersectionPoint23[1].x;
                    result23_2[1] = intersectionPoint23[1].y;

                    ArrayList<DistBetween> DistList = new ArrayList<>();

                    System.out.println(calculateDistance(result12_1,result12_2));

                    DistBetween dist1 = new DistBetween(calculateDistance(result12_1,result12_2),result12_1,result12_2);
                    DistBetween dist2 = new DistBetween(calculateDistance(result12_1,result23_1),result12_1,result23_1);
                    DistBetween dist3 = new DistBetween(calculateDistance(result12_1,result23_2),result12_1,result23_2);
                    DistBetween dist4 = new DistBetween(calculateDistance(result12_2,result23_1),result12_2,result23_1);
                    DistBetween dist5 = new DistBetween(calculateDistance(result12_2,result23_2),result12_2,result23_2);
                    DistBetween dist6 = new DistBetween(calculateDistance(result23_1,result23_2),result23_1,result23_2);
                    DistList.add(dist1);
                    DistList.add(dist2);
                    DistList.add(dist3);
                    DistList.add(dist6);
                    DistList.add(dist5);
                    DistList.add(dist4);



                    double min_dist = dist1.getDistance();
                    for(DistBetween db: DistList){
                        if(db.getDistance()< min_dist){
                            min_dist = db.getDistance();
                        }
                    }

                    for(DistBetween db1: DistList){
                        if(db1.getDistance() == min_dist){
                            coordinate[0] = (int) (db1.getFirst_coor()[0] + db1.getSecond_coor()[0])/2;
                            coordinate[1] = (int) (db1.getFirst_coor()[1] + db1.getSecond_coor()[1])/2;
                        }

                    }

                    System.out.println(Arrays.toString(result12_1));
                    System.out.println(Arrays.toString(result12_2));
                    System.out.println(Arrays.toString(result23_1));
                    System.out.println(Arrays.toString(result23_2));
                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Not enough points of intersection, please find again");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    System.out.println("Not enough points of intersection, please find again");
                }
            }
            catch(Exception e){

                System.out.println("Error in finding your location");
            }

            System.out.println(coordinate[0]);
            System.out.println(coordinate[1]);

            // userCoor = coordinate;

            return userCoor;
        }
        else
        {
            return userCoor; // return coordinate
        }
    }



    public void onGetlocButtonClicked(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            userCoor[0] = 346;
            userCoor[1] = 215;
            getlocButton.setEnabled(false);
            bluetoothDevices.clear();
            addresses.clear();
            bluetoothAdapter.startDiscovery();
        }

    }


    public void plotDots (int[] coordinate){

        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.floorplan2,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawCircle(coordinate[0],coordinate[1],20,paint);

        ImageView imageView = getActivity().findViewById(R.id.map);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);
    }


    public static void providePath(int[] userCoor, int destination, View view){

        //get the nearest node
        //draw to nearest node
        Node nearestNode;
        double minDist = calculateDistance2(userCoor, nodeList.get(0).getCoordinate());
        for (Node n:nodeList){
            double dist = calculateDistance2(userCoor, n.getCoordinate());
            if(dist < minDist)
                minDist = dist;
        }

        System.out.println(nodeList.get(0).getCoordinate()[0]);
        System.out.println(nodeList.get(0).getCoordinate()[1]);

        for(Node n:nodeList){
            if(calculateDistance2(userCoor, n.getCoordinate()) == minDist){
                BitmapFactory.Options myOptions = new BitmapFactory.Options();
                myOptions.inDither = true;
                myOptions.inScaled = false;
                myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.mResources,R.drawable.floorplan2,myOptions);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStrokeWidth(20);
                paint.setColor(Color.BLUE);

                Paint paint1 = new Paint();
                paint1.setAntiAlias(true);
                paint1.setColor(Color.RED);

                Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
                Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888,true);

                Canvas canvas = new Canvas(mutableBitmap);
                canvas.drawCircle(userCoor[0],userCoor[1],20,paint1);
                canvas.drawLine(userCoor[0],userCoor[1] + 10, n.getCoordinate()[0], n.getCoordinate()[1] + 10,paint);

                System.out.println(n.getId());

                String id = n.getId() + Integer.toString(destination);
                System.out.println(id);

                Path path = Path.getPath(id);

                ArrayList<String> allpath = path.getPath();


                ArrayList<Node> allNode = new ArrayList();

                for(int i = 0; i < allpath.size(); i++){
                    for(int j = 0; j < nodeList.size(); j++){
                        if(allpath.get(i).equals(Integer.toString(nodeList.get(j).getId()))){
                            allNode.add(nodeList.get(j));
                        }
                    }
                }


                int k = 0;

                while(k >=0 && k < allNode.size()-1){
                    canvas.drawLine(allNode.get(k).getCoordinate()[0],allNode.get(k).getCoordinate()[1] + 10
                            , allNode.get(k+1).getCoordinate()[0], allNode.get(k+1).getCoordinate()[1] + 10,paint);
                    k++;
                }

                ImageView imageView = view.findViewById(R.id.map);
                imageView.setAdjustViewBounds(true);
                imageView.setImageBitmap(mutableBitmap);

                break;
            }
        }

    }








    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            root_Home = inflater.inflate(R.layout.fragment_home, container, false);


            readNode();
            readPath();

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            getlocButton = root_Home.findViewById(R.id.getlocButton);
            getlocButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGetlocButtonClicked();
                }
            });


            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(broadcastReceiver,intentFilter);


        return root_Home;
    }









    public void readNode(){
        try{
            InputStream is = getActivity().getAssets().open("Node.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null){

                int[] coor = new int[2];
                String[] node = line.split(":");

                int id = Integer.parseInt(node[0]);
                int firstCoor = Integer.parseInt(node[1]);
                int secondCoor = Integer.parseInt(node[2]);

                coor[0] = firstCoor;
                coor[1] = secondCoor;


                Node node1 = new Node(id, coor);
                nodeList.add(node1);

                line = reader.readLine();
            }
        }
        catch (IOException e){
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void readPath(){

        try{
            InputStream is = getActivity().getAssets().open("Path.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line1 = reader.readLine();
            while(line1 != null){

                String[] path = line1.split(":");

                int from = Integer.parseInt(path[0]);
                int to = Integer.parseInt(path[1]);
                int id = Integer.parseInt(path[2]);

                String entirepath = path[3];

                String[] realpath = entirepath.split("->");

                ArrayList<String> path1 = new ArrayList();

                for(int i = 0;i < realpath.length;i++){
                    path1.add(realpath[i]);
                }

                Path finalPath = new Path(from, to, id, path1);
                pathList.add(finalPath);

                line1 = reader.readLine();
            }
        }
        catch (IOException e){
            System.out.println("File not found");
            e.printStackTrace();
        }


        for(Path p: pathList){
            for(Path p1:pathList){
                p.putPath(p1);
            }
        }


    }



}
