package works.myles.smeter;

import android.content.Context;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RPM{

    public LinReg lineOfBestFit = new LinReg();

    public List<List> data = new ArrayList<List>();
    public String[] revolutions = {
            "1000", "2000", "3000", "4000",
            "5000", "6000", "7000", "8000",
            "9000", "10000", "11000", "12000",
            "13000"
    };

    RPM(){
        for(int i = 0; i < revolutions.length; i++) {
            data.add(new ArrayList<String>());
            data.get(i).add(Integer.valueOf(revolutions[i]));
        }
    }

    void save(String filename, Context ctx){

        try {
            calcLinReg();

            PrintWriter writer = new PrintWriter(filename, "UTF-8");

            writer.println(String.valueOf(lineOfBestFit.m));
            writer.println(String.valueOf(lineOfBestFit.b));

            for (int j = 0; j < data.size(); j++ ) {
                for (int k = 0; k < data.get(j).size(); k++){

                    writer.println( data.get(j).get(k).toString());
                    Log.d("RPM", data.get(j).get(k).toString());
                }
            }
            writer.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    void calcLinReg(){
        for (int m = 0; m < data.size(); m++ ){
            for (int n = 0; n < data.get(m).size(); n++){

                if(data.get(m).size() > 1){
                    lineOfBestFit.Ay.add(Integer.valueOf(data.get(m).get(n).toString()));
                    lineOfBestFit.Ax.add(Integer.valueOf(data.get(m).get(0).toString()));
                }
            }
        }
        lineOfBestFit.calcValues();
        Log.d("Line Of Best Fit", String.valueOf(lineOfBestFit.toString()));
        Log.d("M", String.valueOf(lineOfBestFit.m));
        Log.d("B", String.valueOf(lineOfBestFit.b));
    }
}