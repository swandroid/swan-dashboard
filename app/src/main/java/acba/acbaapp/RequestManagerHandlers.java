package acba.acbaapp;

import android.content.Context;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Defines algorithm to be performed upon new results received by a {@link RequestManager}
 */
public interface RequestManagerHandlers {
    void onPostExecute(Context context, String result);
}
