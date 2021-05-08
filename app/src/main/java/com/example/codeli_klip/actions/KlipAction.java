package com.example.codeli_klip.actions;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.codeli_klip.dialog.KlayDlgFragment;
import com.example.codeli_klip.dialog.LinkDlgFragment;
import com.klipwallet.app2app.api.Klip;
import com.klipwallet.app2app.api.KlipCallback;
import com.klipwallet.app2app.api.response.KlipResponse;
import com.klipwallet.app2app.exception.KlipRequestException;

public class KlipAction {
    private Context ctx;
    private Klip klip;

    public KlipAction(Context ctx, Klip klip) {
        this.ctx = ctx;
        this.klip = klip;
    }

    public void prepareLink(KlipCallback<KlipResponse> callback) {
        new LinkDlgFragment(klip, callback).show(((FragmentActivity)ctx).getSupportFragmentManager(), null);
    }

    public void prepareKlay(KlipCallback<KlipResponse> callback) {
        new KlayDlgFragment(klip, callback).show(((FragmentActivity)ctx).getSupportFragmentManager(), null);
    }


    public void request(String requestKey) {
        try {
            klip.request(requestKey);
        } catch (KlipRequestException e) {
            Toast.makeText(ctx, "it's need to call request api first", Toast.LENGTH_LONG).show();
        }
    }

    public void getResult(String requestKey, KlipCallback<KlipResponse> callback) {
        try {
            klip.getResult(requestKey, callback);
        } catch (KlipRequestException e) {
            Toast.makeText(ctx, "it's need to call request api first", Toast.LENGTH_LONG).show();
        }
    }


}
