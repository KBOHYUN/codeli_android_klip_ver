package com.example.codeli_klip.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.codeli_klip.R;
import com.klipwallet.app2app.api.Klip;
import com.klipwallet.app2app.api.KlipCallback;
import com.klipwallet.app2app.api.request.AuthRequest;
import com.klipwallet.app2app.api.request.model.BAppDeepLinkCB;
import com.klipwallet.app2app.api.request.model.BAppInfo;
import com.klipwallet.app2app.exception.KlipRequestException;

public class LinkDlgFragment extends DialogFragment {
    private Klip klip;
    private KlipCallback callback;

    public LinkDlgFragment(Klip klip, KlipCallback callback) {
        this.klip = klip;
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Codeli - Auth")
                .setMessage("사용자 인증 절차를 시작합니다.\n확인을 누르면 인증이 시작됩니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // BApp 정보
                        BAppInfo bAppInfo = new BAppInfo("Codeli");

                        // Auth 정보
                        AuthRequest req = new AuthRequest();

                        try {
                            klip.prepare(req, bAppInfo, callback);
                        } catch (KlipRequestException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
