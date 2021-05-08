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
import com.klipwallet.app2app.api.request.KlayTxRequest;
import com.klipwallet.app2app.api.request.model.BAppDeepLinkCB;
import com.klipwallet.app2app.api.request.model.BAppInfo;
import com.klipwallet.app2app.exception.KlipRequestException;


public class KlayDlgFragment extends DialogFragment {
    private Klip klip;
    private KlipCallback callback;

    public KlayDlgFragment(Klip klip, KlipCallback callback) {
        this.klip = klip;
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dlg_klay, null);
        final EditText to = view.findViewById(R.id.to);
        final EditText from = view.findViewById(R.id.from);
        final EditText klay = view.findViewById(R.id.klay);
        final EditText bappName = view.findViewById(R.id.bappName);

        builder.setView(view);
        builder.setTitle("Codeli - Send KLAY")
                .setMessage("클레이튼 결제를 시작합니다. \n확인을 누르면 결제가 시작됩니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // BApp 정보
                        BAppInfo bAppInfo = new BAppInfo("Codeli");

                        // Klay 정보
                        KlayTxRequest req = new KlayTxRequest.Builder()
                                .to(to.getText().toString())
                                .from(from.getText().toString())
                                .amount(klay.getText().toString())
                                .build();

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
