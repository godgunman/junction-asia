package asia.junction.naruto_go;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggm on 5/7/16.
 */
public class Utils {
    public static List<Point> normalize(List<Point> data, int goalSize) {

        Log.d("debug", "[normalize] size: " + data.size() + " to " + goalSize);

        List<Point> result = new ArrayList<>();

        for (int i = 0; i < goalSize; i++) {
            double ii = ((double) i / ((double) goalSize - 1)) * (data.size() - 1);
            Point point = new Point();

            int floor_ii = (int) Math.floor(ii);
            double part1 = (int) Math.ceil(ii) - ii;

            int ceil_ii = (int) Math.ceil(ii);
            double part2 = ii - (int) Math.floor(ii);

            Log.d("debug", " ii = " + ii + " floor_ii = " + floor_ii + " ceil_ii = " + ceil_ii);

            point.x = data.get(floor_ii).x * part1 + data.get(ceil_ii).x * part2;
            point.y = data.get(floor_ii).y * part1 + data.get(ceil_ii).y * part2;
            point.z = data.get(floor_ii).z * part1 + data.get(ceil_ii).z * part2;

            result.add(point);
        }

        return result;
    }

    public static String trainingDataToString(List<Point> data) {

        String result = "1"; // label
        int index = 1;

        for (int i = 0; i < data.size(); i++) {
            result += String.format(" %d:%f", index++, data.get(i).x);
            result += String.format(" %d:%f", index++, data.get(i).y);
            result += String.format(" %d:%f", index++, data.get(i).z);
        }

        return result;
    }
}
