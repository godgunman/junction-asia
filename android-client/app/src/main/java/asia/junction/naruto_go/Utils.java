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
        Log.d("OriginalSize", String.valueOf(data.size()));
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
            Point p = data.get(i);
            double absX = Math.abs(p.x);
            double absY = Math.abs(p.y);
            double absZ = Math.abs(p.z);
            if (absX >= absY && absX >= absZ) {
                result += String.format(" %d:%d", index++, (int)(absX/p.x));
            }
            else
                result += String.format(" %d:0", index++);
            if (absY >= absX && absY >= absZ) {
                result += String.format(" %d:%d", index++, (int)(absY/p.y));
            }
            else
                result += String.format(" %d:0", index++);
            if (absZ >= absX && absZ >= absY) {
                result += String.format(" %d:%d", index++, (int)(absZ/p.z));
            }
            else
                result += String.format(" %d:0", index++);
        }

        return result;
    }

    public static void sendResultToServer(int result) {
        String url = "";

        switch (result) {
            case -1:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/switch").buildUpon()
                        .appendQueryParameter("mode", "game")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
                break;
            case 1:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/switch").buildUpon()
                        .appendQueryParameter("mode", "prev")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
                break;
            case 4:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/temp").buildUpon()
                        .appendQueryParameter("adjust", "-1")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
                break;
            case 2:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/switch").buildUpon()
                        .appendQueryParameter("mode", "next")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
                break;
            case 3:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/temp").buildUpon()
                        .appendQueryParameter("adjust", "1")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
                break;
            default:
                url = Uri.parse("http://192.168.43.83:1337/sensor/change/switch").buildUpon()
                        .appendQueryParameter("mode", "next")
                        .appendQueryParameter("name", "ggm")
                        .build().toString();
        }

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
