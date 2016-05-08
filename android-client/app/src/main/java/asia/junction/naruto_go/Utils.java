package asia.junction.naruto_go;

import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ggm on 5/7/16.
 */
public class Utils {
    public static List<Point> normalize(List<Point> data, int goalSize) {
        List<Point> result = new ArrayList<>();

        if (data.size() == 0) return result;

        for (int i = 0; i < goalSize; i++) {
            double ii = ((double) i / ((double) goalSize - 1)) * (data.size() - 1);
            Point point = new Point();

            int floor_ii = (int) Math.floor(ii);
            int ceil_ii = (int) Math.ceil(ii);
            double part2 = ii - (int) Math.floor(ii);
            double part1 = 1 - part2;

            point.x = data.get(floor_ii).x * part1 + data.get(ceil_ii).x * part2;
            point.y = data.get(floor_ii).y * part1 + data.get(ceil_ii).y * part2;
            point.z = data.get(floor_ii).z * part1 + data.get(ceil_ii).z * part2;

            result.add(point);
        }

        return result;
    }

    public static String dataToString(List<Point> data, String label) {

        String result = label; // label
        int index = 1;

        for (int i = 0; i < data.size(); i++) {
            result += String.format(" %d:%f", index++, data.get(i).x);
            result += String.format(" %d:%f", index++, data.get(i).y);
            result += String.format(" %d:%f", index++, data.get(i).z);
        }

        return result;
    }

    public static void sendResultToServer(int result) {
        switch (result) {
            default:
                String url = Uri.parse("http://192.168.43.83:1337/sensor/change").buildUpon()
                        .appendQueryParameter("temp", "25")
                        .appendQueryParameter("icon", "cool")
                        .appendQueryParameter("time", "2015/05/05")
                        .appendQueryParameter("text", "ggm")
                        .build().toString();

                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("[sendResultToServer]", new String(response));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });
        }
    }
}
