package com.app.shopifyuser.shared;

import android.content.Context;


public class Session {
    private static Session instance = null;
    private static LocalSave localSave = null;

    private Context mContext;


    private Session(Context mContext) {
        this.mContext = mContext;
    }

    public static Session getInstance(Context context) {
        if (instance == null) {
            instance = new Session(context);
            localSave = LocalSave.getInstance(context);

        }
        return instance;
    }

    public LocalSave getLocalSave() {
        return localSave;
    }

    private Context getmContext() {
        return mContext;
    }


    public String getCurrentUserToken()
    {

        return getLocalSave().getUserToken();
    }




    public void clear() {
        localSave.clear();
    }
}
