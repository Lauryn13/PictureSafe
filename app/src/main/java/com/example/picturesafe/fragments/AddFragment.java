package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeCheckBox;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeDropDown;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.CompressionType;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeDataMissingException;
import com.example.picturesafe.exceptions.PictureSafeDataWontFitInImageException;
import com.example.picturesafe.exceptions.PictureSafeMissingPasswordException;
import com.example.picturesafe.exceptions.PictureSafeNotAllPicturesUsedInfo;

import java.io.IOException;
import java.util.Arrays;


/** Fragment, welches den Tab der ADD Seite darstellt. (Um Daten in ein Bild abspeichern zu können)
 *  Kümmert sich um das abspeichern von Daten in den ausgewählten Bildern und lädt Datein vom Handy.
 */
public class AddFragment extends Fragment {
    private ActivityResultLauncher<Intent> pickFileLauncher; // Intent-Launcher zur auswahl einer Datei
    MainViewModel mvm; // ViewModel zum zwischenspeichern von Daten

    // UI Komponenten
    PictureSafeButton btnSelectFile;
    PictureSafeButton btnWrite;
    PictureSafeText fileText;
    PictureSafeEditText saveableText;
    PictureSafeDropDown compressionDropDown;
    PictureSafeCheckBox encryptionCheckBox;
    PictureSafeEditText passwordText;

    private AlertDialog loadingDialog;

    /** onCreate
     *  Setzt das onActivityResult für diesen Tab
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pickFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                try {
                    // Versucht ein File zum abspeichern zu laden
                    this.loadFile(result.getData().getData());
                } catch (PictureSafeBaseException e){
                    // Anzeigen der eventuell geworfenen Exception
                    PictureSafeDialog.show(getParentFragmentManager(), e);
                }
            }
        });
    }

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
     * @return Return the View for the fragment's UI, or null.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // ViewModel erstellen/laden
        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.mvm.dataChoosen = true;

        // UI-Komponenten initialisieren
        this.btnSelectFile = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnSelectFile), true);
        this.btnWrite = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnWrite));
        this.fileText = new PictureSafeText(view.findViewById(R.id.fileText), view.findViewById(R.id.fileCard), true);
        this.saveableText = new PictureSafeEditText(view.findViewById(R.id.saveableText), view.findViewById(R.id.saveableCard));
        this.compressionDropDown = new PictureSafeDropDown(requireContext(), view.findViewById(R.id.compressionSpinner), true);
        this.encryptionCheckBox = new PictureSafeCheckBox(view.findViewById(R.id.textEncryption), view.findViewById(R.id.checkBoxEncrytion), true);
        this.passwordText = new PictureSafeEditText(view.findViewById(R.id.passwordText), view.findViewById(R.id.passwordCard));

        TextView tabFile = view.findViewById(R.id.tabFile);
        TextView tabText = view.findViewById(R.id.tabText);

        // OnClick für den Button zum Laden eines Files erstellen
        // erstellt einen Intent, welcher dann in der RegisterForActivityResult verarbeitet wird
        this.btnSelectFile.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Erlaubte Datentypen
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                    "text/plain",
                    "image/jpeg",
                    "image/png",
                    "audio/mpeg",
                    "video/mp4",
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "text/csv",
                    "application/zip"
            });
            intent.setType("*/*");

            this.pickFileLauncher.launch(Intent.createChooser(intent, "Datei auswählen"));
            this.btnSelectFile.changeText("Neue Datei auswählen");
        });

        // OnClick für den Button zum Schreiben der Dateien erstellen
        this.btnWrite.button.setOnClickListener(v -> {
            showLoadingDialog();

            // Einmal UI Update abwarten (da kein Threading genutzt wird und sonst Loading Dialog nicht angezeigt wird.
            requireView().post(() -> {
                try {
                    clickBtnWrite();
                    // Wartende Information, die im Verlauf des Schreibens aufgetreten sind
                    if(this.mvm.waitingException != null)
                        throw this.mvm.waitingException;
                    hideLoadingDialog();
                } catch (PictureSafeBaseException e) {
                    hideLoadingDialog();
                    // Anzeigen des Dialogs mit der Exception
                    PictureSafeDialog.show(getParentFragmentManager(), e);
                }catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Zurücksetzen der Wartenden Exception, sollte irgendeine geworfen wurden sein
                    this.mvm.waitingException = null;
                }
            });
        });

        // Anzeigen des Passwort-Edits sollte die Checkbox aktiviert sein
        this.encryptionCheckBox.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> this.passwordText.changeVisibility(isChecked));

        // Auswahl für den TAB Datei, welcher die Entsprechenden UI Komponenten darunter lädt
        // = Man möchte eine Datei im Bild speichern
        tabFile.setOnClickListener(v -> {
            this.mvm.dataChoosen = true;
            this.btnSelectFile.changeVisibility(true);
            this.saveableText.changeVisibility(false);

            // Setzen des anzuzeigenen Textes
            if(this.mvm.fileData != null){
                float storable_mb = 0;
                if(this.mvm.pictures != null) {
                    // Bilder und Datei wurde ausgewählt -> Infotext wird generiert
                    for (Picture pic : this.mvm.pictures)
                        storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;

                    float fileLength = (float) Math.round((float) this.mvm.fileData.content.length / 1000 / 10) / 100;
                    this.fileText.setText(this.mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
                    this.btnWrite.changeVisibility(true);
                }
                else
                    this.fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + this.mvm.fileData.name);
            }
            else
                this.btnWrite.changeVisibility(false);

            tabFile.setBackgroundColor(requireContext().getColor(R.color.primaryVariant));
            tabText.setBackgroundColor(requireContext().getColor(R.color.primary));
        });

        // Auswahl für den TAB Text, welcher die Entsprechenden UI Komponenten darunter lädt
        // = Man möchte einen Text im Bild speichern
        tabText.setOnClickListener(v -> {
            this.mvm.dataChoosen = false;
            this.saveableText.changeVisibility(true);
            this.btnSelectFile.changeVisibility(false);
            this.btnWrite.changeVisibility(true);
            this.fileText.removeText();

            tabText.setBackgroundColor(requireContext().getColor(R.color.primaryVariant));
            tabFile.setBackgroundColor(requireContext().getColor(R.color.primary));
        });

        if(this.mvm.pictures == null) {
            // Keine Bilder als Grundlage ausgewählt -> Button zum schreiben nicht anzeigen
            this.btnWrite.changeVisibility(true);
        }

        // Infotext der Datei anzeigen
        if(this.mvm.fileData != null){
            float storable_mb = 0;
            if(this.mvm.pictures != null) {
                for (Picture pic : this.mvm.pictures)
                    storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;

                float fileLength = (float) Math.round((float) this.mvm.fileData.content.length / 1000 / 10) / 100;
                this.fileText.setText(this.mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
            }
            else
                this.fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + this.mvm.fileData.name);
            this.btnSelectFile.changeText("Neue Datei auswählen");
            this.btnWrite.changeVisibility(true);
        }
        else{
            if(this.mvm.pictures != null) {
                float storable_mb = 0;
                for (Picture pic : this.mvm.pictures)
                    storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;

                this.fileText.setText("Keine Speicherbare Datei ausgewählt! \nMaximal Speicherbare Größe: " + storable_mb + " Mb");
            }
            else
                this.fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!");


            this.btnWrite.changeVisibility(false);
        }


        return view;
    }

    /** clickBtnWrite
     *  Funktion um eine Datei/Text in den Bildern speichern zu können.
     *  Wird ausgeführt, wenn der Schreiben Button gedrückt wird.
     */
    public void clickBtnWrite() throws IOException{
        CompressionType compressionType = CompressionType.fromUI(this.encryptionCheckBox, this.compressionDropDown);

        // Es soll Text gespeichert werden -> Muss eine FileData imitiert werden, welche die Daten des Textes beinhaltet
        if(!this.mvm.dataChoosen){
            String textData = this.saveableText.readText();
            this.mvm.fileData = new FileData(textData.getBytes(), DataTypes.TEXTDATA, "textExport", textData.getBytes().length);
        }

        compressionType = this.mvm.fileData.compressData(compressionType);

        // zu speichernden Byte-Daten
        byte[] byteData = this.mvm.fileData.content;

        // Lesen des Kompressionstypes und des Passworts aus den UI-Komponenten
        char[] password = this.passwordText.readText().toCharArray();

        // Verschlüsselung ausgewählt, ohne Passwort zu definieren
        if(compressionType.usesEncryption() && password.length == 0)
            throw new PictureSafeMissingPasswordException();

        // Sicherheit: Kein Bild als Grundlage ausgewählt (sollte nicht passieren weil der Button dann deaktiviert wurde)
        if(this.mvm.pictures == null || this.mvm.pictures.length == 0)
            throw new PictureSafeDataMissingException("Es wurden keine Bilder als Grundlage zum speichern ausgewählt.");

        // Setzen der Daten in den Bilder
        if(byteData.length <= this.mvm.pictures[0].storeable_data_in_byte){
            // Daten passen in einem Bild rein
            try {
                // Daten im Bild schreiben und das Bild generieren (abspeichern auf dem Gerät)
                this.mvm.pictures[0].setData(this.mvm, byteData, this.mvm.fileData.uncompressedDataLength, 1, this.mvm.fileData.dataType, compressionType, password, this.mvm.fileData.name);
                this.mvm.pictures[0].generatePng(requireContext());
            } catch (IOException e){
                // Fehlerdialog wird aufgerufen (Sicherheitshalber werden alle Passworteingaben und Variablen gelöscht)
                this.passwordText.clearText();
                Arrays.fill(password, '\0');
                throw new RuntimeException(e);
            }

            // Es wurden mehr als 1 Bild ausgewählt (aber nur in einem gespeichert) -> Information an den User
            if(this.mvm.pictures.length != 1)
                throw new PictureSafeNotAllPicturesUsedInfo();
        }
        else{
            // Daten passen nicht in ein Bild hinein
            int spaceInPictures = 0; // gesamter Speicherplatz aller Bilder
            int[] spacesPerPicture = new int[this.mvm.pictures.length]; // Speicherplatz von jedem Bild + den Bildern davor -> Berechnung wie viele Bilder gebraucht werden

            // Berechnung des gesamt verfügbaren Speicherplatzes in den Bildern
            for(Picture pic : this.mvm.pictures) {
                spaceInPictures += pic.storeable_data_in_byte;
                spacesPerPicture[pic.currentPicture - 1] = spaceInPictures;
            }

            // Daten passen nicht in die verfügbaren Bilder hinein
            if(spaceInPictures < this.mvm.fileData.content.length)
                throw new PictureSafeDataWontFitInImageException(this.mvm.fileData.content.length, spaceInPictures);

            int neededPictures = 1; // minimal gebrauchte Bilder
            int offset = 0; // Länge der schon gespeicherten Datenbytes in vorherigen Bildern

            // Auswählen der minimal benötigten Bilder
            while(spacesPerPicture[neededPictures - 1] < byteData.length)
                neededPictures++;

            // Schreiben der Daten
            for(int i = 0; i < neededPictures; i++) {
                // Aufspalten der Daten in einzelnen Chunks, die in die Bilder hineinpassen
                int length = Math.min(this.mvm.pictures[i].storeable_data_in_byte, byteData.length - offset);
                byte[] saveableByteData = new byte[length];

                // Generieren des Chunks
                System.arraycopy(byteData, offset, saveableByteData, 0, length);
                try {
                    // Schreiben eines Chunks in dem Bild und generieren (speichern) des neuen Fotos
                    this.mvm.pictures[i].setData(this.mvm, saveableByteData, this.mvm.fileData.uncompressedDataLength, neededPictures, this.mvm.fileData.dataType, compressionType, password, this.mvm.fileData.name);
                    this.mvm.pictures[i].generatePng(requireContext());
                } catch (IOException e){
                    // Fehler beim Schreiben/Lesen  -> Sicherheitshalber alle Passworteingaben und Variablen löschen
                    this.passwordText.clearText();
                    Arrays.fill(password, '\0');
                    throw new RuntimeException(e);
                }

                offset += length;
            }

            // Information an den User, dass nicht alle Bilder für das Speichern benötigt wurden.
            if(this.mvm.pictures.length > neededPictures)
                throw new PictureSafeNotAllPicturesUsedInfo();
        }

        // Passworteingaben löschen
        this.passwordText.clearText();
        Arrays.fill(password, '\0');
    }

    /** loadFile
     *  Laden einer Datei aus dem Handy.
     *
     * @param data URI des ausgewählten Files
     */
    public void loadFile(Uri data) {
        if (data == null)
            return;

        try {
            // Laden der Datei als FileData objekt
            this.mvm.fileData = new FileData(requireContext(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // UI anpassen, sollten Daten erfolgreich geladen werden
        if(this.mvm.pictures == null)
            this.fileText.setText("Bitte wähle zunächst ein Bild zum Speichern aus!\n Dateiname: " + this.mvm.fileData.name);
        else{
            float storable_mb = 0;
            for (Picture pic : this.mvm.pictures)
                storable_mb += ((float) Math.round((float) pic.storeable_data_in_byte / 1000 / 10)) / 100;

            float fileLength = (float) Math.round((float) this.mvm.fileData.content.length / 1000 / 10) / 100;
            this.fileText.setText(this.mvm.fileData.name + "\n\nGröße: " + fileLength + " Mb \n Maximal Speicherbare Größe: " + storable_mb + " Mb");
            this.btnWrite.changeVisibility(true);
        }
    }

    /** showLoadingDialog
     *  Zeigt eine Ladeanzeige an
     */
    private void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);

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