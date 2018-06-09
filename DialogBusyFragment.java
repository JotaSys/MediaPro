package cu.ij.jotalab.mediapro;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by jota on 4/11/2018.
 */

public class DialogBusyFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBusy = new AlertDialog.Builder(getActivity());
        LayoutInflater inflaterBusy = getActivity().getLayoutInflater();
        View dialog_busy = inflaterBusy.inflate(R.layout.dialog_busy,null);
        dialog_busy.setBackgroundColor(R.color.colorTransparent);

        dialogBusy.setView(dialog_busy);

        return dialogBusy.create();
    }

}
