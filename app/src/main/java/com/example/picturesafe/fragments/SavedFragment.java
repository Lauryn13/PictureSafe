package com.example.picturesafe.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeDataMissingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/** SavedFragment
 *  Anzeige der gespeicherten Daten aus den Bildern
 *  Ermöglicht den Export der gespeicherten Daten wieder zur ursprünglichen Datei.
 */
public class SavedFragment extends Fragment {
    MainViewModel mvm; // ViewModel zum zwischenspeichern von Daten

    // UI Komponenten
    PictureSafeImage outputImage;
    PictureSafeButton btnExport;
    PictureSafeButton btnDecrypt;
    PictureSafeText readedText;

    private AlertDialog loadingDialog;

    /** onCreateView
     *  Erstellt die Angezeigten UI-Componenten im Frontend und setzt deren Funktionen und Werte.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Aktuelle View
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        // ViewModel erstellen/laden
        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // UI-Komponenten initialisieren
        this.btnExport = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnExport));
        this.btnDecrypt = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnDecrypt));
        this.readedText = new PictureSafeText(view.findViewById(R.id.readedText), view.findViewById(R.id.readedCard));
        this.outputImage = new PictureSafeImage(view.findViewById(R.id.outputImage), view.findViewById(R.id.outputCard));

        // OnClick für den Button zum Export der gespeicherten Daten erstellen
        this.btnExport.button.setOnClickListener(v -> {
            showLoadingDialog();

            // Einmal UI Update abwarten (da kein Threading genutzt wird und sonst Loading Dialog nicht angezeigt wird.
            requireView().post(() -> {
                try {
                    clickBtnExport();
                    hideLoadingDialog();
                } catch (PictureSafeBaseException e) {
                    hideLoadingDialog();
                    PictureSafeDialog.show(getParentFragmentManager(), e);
                }
            });
        });

        // OnClick für den Button zum Entschlüsseln der Daten erstellen (sollt der Entschlüsselvorgang beim Lesen abgebrochen werden
        this.btnDecrypt.button.setOnClickListener(v -> {
            showLoadingDialog();

            // Einmal UI Update abwarten (da kein Threading genutzt wird und sonst Loading Dialog nicht angezeigt wird.
            requireView().post(() -> {
                try {
                    clickBtnDecrypt();
                    hideLoadingDialog();
                } catch (PictureSafeBaseException e){
                    hideLoadingDialog();
                    PictureSafeDialog.show(getParentFragmentManager(), e);
                }
            });
        });

        // Anzeigen der Daten
        showImage();
        return view;
    }

    /** clickBtnExport
     *  Export der Daten aus den Bilder in eine Datei und speichern der Datei auf dem Gerät.
     */
    public void clickBtnExport(){
        if(this.mvm.fileData != null){
            Uri uri;
            try {
                uri = this.mvm.fileData.exportFile(requireContext());
            } catch (IOException e){
                throw new RuntimeException(e);
            }
            Objects.requireNonNull(uri);
        } else
            // Sollte nicht passieren, da der Button nur angezeigt wird wenn Daten vorhanden sind (sicherheit)
            throw new PictureSafeDataMissingException("Es wurden keine Daten zum abspeichern im Bild angegeben.");
    }

    /** clickBtnDecrypt
     *  Entschlüsseln der Daten, sollte der Entschlüsselungsprozess beim Lesen abgebrochen werden.
     */
    public void clickBtnDecrypt(){
        showPasswordDialog(password -> {
            try {
                this.readFileData(password);
                showImage();
            } catch (PictureSafeBaseException e) {
                PictureSafeDialog.show(getParentFragmentManager(), e);
            }
            // Passwort nach nutzung überschreiben
            Arrays.fill(password, '0');
        });
    }

    /** showImage (vielleicht verwirrender Name)
     *  Anzeigen der gespeicherten Daten, je nach Datentyp
     */
    public void showImage(){
        this.btnDecrypt.changeVisibility(false);

        // Text wird je nach aktueller Lage der Daten gesetzt um dem User Informationen über den Status zu zeigen.
        if(this.mvm.pictures == null)
            this.readedText.setText("Bitte wähle zunächst ein Foto zum lesen aus.");
        else if(this.mvm.picturesAreIncomplete)
            this.readedText.setText("Zum wiederherstellen der Daten sind nicht alle Bilder vorhanden.");
        else if (this.mvm.picturesHasData && this.mvm.picturesDataIsCorrupted)
            this.readedText.setText("Die Inhalte des Bildes wurden überschrieben und dadurch zerstört.");
        else if(this.mvm.picturesHasData && this.mvm.fileData == null) {
            this.readedText.setText("Der gespeicherte Inhalt ist zurzeit noch Verschlüsselt.");
            this.btnDecrypt.changeVisibility(true);
        }
        else if(!this.mvm.picturesHasData)
            this.readedText.setText("Dieses Bild hat keinen gespeicherten Inhalt.");
        else{
            // eine gespeicherte Datei wurde gefunden und erfolgreich geladen
            switch (this.mvm.storedDataType){
                case JPG:
                case PNG:
                    // Bilder werden als Image angezeigt
                    Bitmap outputBitmap = BitmapFactory.decodeByteArray(this.mvm.fileData.content, 0, this.mvm.fileData.content.length);
                    this.outputImage.setImage(outputBitmap);
                    this.readedText.setText(this.mvm.fileData.name);
                    break;
                case TEXTDATA:
                    // Texte werden in einem Text angezeigt
                    String text = new String(this.mvm.fileData.content);
                    this.readedText.setText(text);
                    break;
                default:
                    // Daten können nicht angezeigt werden
                    this.readedText.setText(this.mvm.fileData.name + "\n\nBisher nicht unterstützter Datentyp. Er kann hier zwar nicht angezeigt, jedoch exportiert werden.");
                    break;
            }

            // Sollte es sich nicht um ein Text handeln, kann ein Export erstellt werden
            if(this.mvm.storedDataType != DataTypes.TEXTDATA)
                this.btnExport.changeVisibility(true);
        }
    }

    /** readFileData
     *  Laden der Daten aus den ausgewählten Bildern.
     *
     * @param password Passwort der verschlüsselten Daten
     */
    private void readFileData(char[] password){
        String name = null;
        byte[][] data = new byte[this.mvm.pictures.length][];
        int completeDataLength = 0;
        int picturesWithData = 0;
        String signature = null;
        DataTypes dataType = null;
        int uncompressedDataLength = -1;

        // Herausfinden welche Bilder tatsächlich Daten haben
        // Das erste Bild, was Daten enthält wird als "Hauptbild" gesetzt -> ID/Signatur wird auf dieses Bild gesetzt und nur noch Bilder akzeptiert, die Inhalte zur gleichen Datei hat
        for(int i = 0; i < this.mvm.pictures.length; i++){
            Picture picture = this.mvm.pictures[i];
            if(picture.hasData){
                // überprüfen der Signatur ob sie gleich sit
                if(signature != null && !signature.equals(picture.signature))
                    continue;
                else if(signature == null)
                    // Signatur wird gesetzt -> nur zugehörige Bilder können weiter verwendet werden
                    signature = picture.signature;
                    dataType = picture.storedDataType;
                    uncompressedDataLength = picture.savedDataLength;

                try {
                    // Versuchen die Daten der Bilder in data zu speichern (je nach Index des Bildes)
                    data[picture.currentPicture - 1] = picture.readContent(password).content;
                } catch (IndexOutOfBoundsException e){
                    // Nicht alle Daten zum wiederherstellen der Bilder sind vorhanden
                    this.mvm.picturesAreIncomplete = true;
                    return;
                }
                name = picture.name;
                completeDataLength += picture.savedDataLength;
                picturesWithData = picture.amountOfPictures;
            }
        }

        byte[] completeData = new byte[completeDataLength]; // Vollständige Daten

        // Überprüfen ob alle nötigen Daten zum wiederherstellen da sind
        if(picturesWithData > data.length){
            this.mvm.picturesAreIncomplete = true;
            return;
        }
        for(int i = 0; i < picturesWithData; i++){
            if(data[i] == null){
                this.mvm.picturesAreIncomplete = true;
                return;
            }
        }

        // Zusammenfügen der Daten aus den einzelnen Arrays zu einem Byte-Array
        int pos = 0;
        for(byte[] b : data){
            if(b == null)
                break;

            System.arraycopy(b, 0, completeData, pos, b.length);
            pos += b.length;
        }

        // erstellen des FileData-Objektes mit den zusammengefügten Daten
        this.mvm.fileData = new FileData(completeData, dataType, name, uncompressedDataLength);
    }

    /** showPasswordDialog
     *  Zeigt ein Dialog an um das Passwort zu eingeben.
     *
     * @param onPassword Passwort zum auslesen der Daten
     */
    private void showPasswordDialog(Consumer<char[]> onPassword) {
        hideLoadingDialog();
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.password_dialog, null);
        PictureSafeEditText pwEdit = new PictureSafeEditText(view.findViewById(R.id.passwordText), view.findViewById(R.id.passwordCard), true);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Verschlüsselte Daten");
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("Entschlüsseln", (d, w) -> {
            char[] pw = pwEdit.readText().toCharArray();
            showLoadingDialog();
            requireView().post(() -> {
                onPassword.accept(pw);
                hideLoadingDialog();
                pwEdit.clearText();
            });
        });
        builder.setNegativeButton("Abbrechen", (d, w) -> {
            pwEdit.clearText();
        });
        builder.show();
    }

    /** showLoadingDialog
     *  Zeigt eine Ladeanzeige an
     */
    private void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) return;

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.loading_dialog, null);

        loadingDialog = new AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create();
        loadingDialog.show();
    }

    /** hideLoadingDialog
     *  Versteckt die Ladeanzeige, wenn die Backendaufgaben abgeschlossen sind.
     */
    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss(); // Schließen des Dialogs
            loadingDialog = null;
        }
    }
}