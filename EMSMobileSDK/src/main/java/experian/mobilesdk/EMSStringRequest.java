package experian.mobilesdk;

import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.lang.reflect.Field;

/**
 * Created by C17045A on 7/10/2017.
 */

class EMSStringRequest extends StringRequest {
    private static final int TIMEOUT = 1500;
    private static final int MAX_RETRY = 3;

    public EMSStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.setTag(0);
    }

    @Override
    public void deliverError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            int attempts = (int)this.getTag();
            if (attempts == MAX_RETRY) {
                super.deliverError(error);
            }
            else
            {
                attempts++;
                this.setTag(attempts);
                try {
                    Field mRequestQueue = this.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("mRequestQueue");
                    mRequestQueue.setAccessible(true);
                    Thread.sleep((attempts* TIMEOUT)*attempts);
                    RequestQueue queue = (RequestQueue)mRequestQueue.get(this);
                    queue.add(this);
                }
                catch (NoSuchFieldException nofEx)
                {
                    super.deliverError(error);
                }
                catch (InterruptedException intEx)
                {
                    super.deliverError(error);
                }
                catch (IllegalAccessException ex) {
                    Log.d("TAG", "Unable to get queue for retry");
                }
            }
        }
        else
        {
            super.deliverError(error);
        }
    }
}
