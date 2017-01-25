package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.util.Collections.sort;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.graph)
    GraphView graph;
    private int maxRange;
    private ArrayList<Double> priceArray;
    private ArrayList<Double> Xs;
    private LineGraphSeries<DataPoint> series;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String Historicaldata= intent.getStringExtra("Data");
        String [] HistoricaldataArray=Historicaldata.split("\\r?\\n");
        String symbolText = intent.getStringExtra(getString(R.string.Sympol_name));
        setTitle(symbolText);

        fillGraph(HistoricaldataArray);
    }

    private void fillGraph(String[] historicaldataArray) {
      priceArray = new ArrayList<Double>();
        Xs = new ArrayList<Double>();
        for (int i = 0; i < historicaldataArray.length; i++) {
            String pair[] = historicaldataArray[i].split(", ");
            Double X = Double.valueOf(pair[0]);
            Double price = Double.valueOf(pair[1]); //y
            Xs.add(X/100000000); //x's
            priceArray.add(price); //y's
        }
          sort(Xs);
          findRange(priceArray);
          series = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < historicaldataArray.length; i++){
            String pair[] = historicaldataArray[i].split(", ");
            Double price = (Double.valueOf(pair[1])); //y
            Double X=(Xs.get(i));//x
            DataPoint point=new DataPoint(X,price);
            series.appendData(point,true,maxRange,false);
                    }
        designgraph();
    }
    private void designgraph(){
        series.setThickness(5);
        series.setDrawBackground(true);
        series.setColor(Color.parseColor("#7C4DFF"));
        series.setBackgroundColor(Color.parseColor("#9E9E9E"));
        graph.addSeries(series);


    }
    public void findRange(ArrayList<Double> mArrayList)
    {
        maxRange = (int) Math.round(Collections.max(mArrayList));
        int n = 10;
        if(maxRange >= 100){
            n = 100;
        }
        else if(maxRange >=1000){
            n=1000;
        }
        if(maxRange%n != 0){
            maxRange += (n - (maxRange%n));
        }
    }

}


