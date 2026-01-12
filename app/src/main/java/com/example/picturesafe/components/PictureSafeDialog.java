package com.example.picturesafe.components;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.picturesafe.R;
import com.example.picturesafe.exceptions.PictureSafeBaseException;

import java.util.Objects;

/** PictureSafeDialog
 *  Dialogfenster das im Frontend geöffnet werden kann um Fehlermeldungen o.ä. anzuzeigen
 */
public class PictureSafeDialog extends DialogFragment {
    /** show
     *  Erstellt das Fenster und zeigt es an
     *
     * @param fm FragmentManager (aktuelles Fragment was angezeigt wird im Frontend)
     * @param e Exception die angezeigt werden soll
     */
    public static void show(FragmentManager fm, PictureSafeBaseException e) {
        Bundle b = new Bundle();
        b.putString("title", e.message);
        b.putString("description", e.description);
        b.putBoolean("info", e.isInformation);

        PictureSafeDialog dialog = new PictureSafeDialog();
        dialog.setArguments(b);
        dialog.show(fm, "GLOBAL_DIALOG");
    }

    /** onCreateDialog
     *  Erstellt das Fenster mit den übergebenen Informationen
     *
     * @param saved
     * @return Dialogfenster
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle saved) {
        boolean info = Objects.requireNonNull(getArguments()).getBoolean("info");

        return new AlertDialog.Builder(requireContext())
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("description"))
                .setIcon(info ? R.drawable.ic_info : R.drawable.ic_error)
                .setPositiveButton("OK", null)
                .create();
    }
}
