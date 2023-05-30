package works.myles.smeter;

import java.util.ArrayList;

public class LinReg{

    /**
     * Least Squares Regression Explanation
     *
     * Line of Best Fit:
     * y = mx + b
     *
     * let:
     *      y = how far up (coordinate plane height)
     *      x = how far along (coordinate plane weight)
     *      m = Slope or Gradient
     *      b = the Y - Intercept
     *      Σ = sum of all values in set '*'
     *
     * Step 1:	For each (x,y) point calculate x2 and xy
     *
     * Step 2:	Sum all x, y, x2 and xy, which gives us Σx, Σy, Σx2 and Σxy
     *
     * Step 3:	Calculate Slope m:
     *
     *          m =  ( N Σ(xy) − Σx Σy ) / ( N Σ(x2) − (Σx)2 )
     *
     *               (N is the number of points.)
     *
     * Step 4:	Calculate Intercept b:
     *
     *          b =  ( Σy − m Σx ) / N
     *
     * Step 5: Assemble the equation of a line
     *
     *          y = mx + b
     *
     * Done!
     *
     * **/

    ArrayList<Integer> Ax = new ArrayList<Integer>();
    ArrayList<Integer> Ay = new ArrayList<Integer>();

    double sumX = 0, sumY = 0, sumX2 = 0, sumXY = 0, m = 0, b = 0;
    int N = 0;

    LinReg(){}

    public void calcValues(){
        sumX = sumX();
        sumY = sumY();
        sumXY = sumXY();
        sumX2 = sumX2();
        N = Ax.size();
        m = calcSlope();
        b = calcIntercept();
    }

    // Construction and Calculation Methods
    public int sumX(){
        int xSum = 0;
        for(Integer tempX : Ax)
            xSum += tempX;
        return xSum;
    }

    public int sumY(){
        int ySum = 0;
        for(Integer tempY : Ay)
            ySum += tempY;
        return ySum;
    }

    public int sumX2(){
        int xSum = 0;
        for(Integer tempX : Ax)
            xSum += Math.pow(tempX, 2);
        return xSum;
    }

    public int sumXY(){
        int xySum = 0;
        for(int i = 0; i < N; i++)
            xySum += ( Ax.get(i) * Ay.get(i));
        return xySum;
    }

    public double calcSlope(){
        double newM = ( ( N * sumXY ) - ( sumX * sumY ) )
                    / ( ( N * sumX2 ) - (Math.pow(sumX,2)));
        return Math.round(newM * 100.0) / 100.0;
    }

    public double calcIntercept(){
        return ( Math.round( ( ( sumY - ( m * sumX ) ) / N ) * 100.0) / 100.0 );
    }

    public String equation(){ return ( "y = " + m + "x + " + b ); }
}