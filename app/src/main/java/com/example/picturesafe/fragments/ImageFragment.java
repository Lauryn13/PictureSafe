package com.example.picturesafe.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.picturesafe.MainViewModel;
import com.example.picturesafe.R;
import com.example.picturesafe.classes.FileData;
import com.example.picturesafe.classes.Picture;
import com.example.picturesafe.classes.PictureUtils;
import com.example.picturesafe.components.PictureSafeButton;
import com.example.picturesafe.components.PictureSafeDialog;
import com.example.picturesafe.components.PictureSafeEditText;
import com.example.picturesafe.components.PictureSafeImage;
import com.example.picturesafe.components.PictureSafeText;
import com.example.picturesafe.enumerators.DataTypes;
import com.example.picturesafe.exceptions.PictureSafeBaseException;
import com.example.picturesafe.exceptions.PictureSafeFileNotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** ImageFragment
 *  Auswahl des Bildes/der Bilder, die Daten enthalten könnten bzw. die Daten speichern sollen.
 *  Werden dann in der UI alle angezeigt und können fokussiert werden sollen.
 */
public class ImageFragment extends Fragment {
    MainViewModel mvm; // ViewModel zum zwischenspeichern von Daten
    private ActivityResultLauncher<Intent> pickImageLauncher; // Intent-Launcher zur Auswahl von Bildern
    // UI Komponenten
    PictureSafeButton btnSelectPicture;
    PictureSafeImage imagePreview;
    PictureSafeText infoText;
    PictureSafeButton btnReset;
    LinearLayout thumbnailContainer;


    /** onCreate
     *  Setzt das onActivityResult für diesen Tab
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // Liste an URIS der Bilder
                List<Uri> uris = new ArrayList<>();

                if (result.getData().getClipData() != null) {
                    // mehrere Bilder sind ausgewählt (alle werden geladen)
                    ClipData clip = result.getData().getClipData();
                    for (int i = 0; i < clip.getItemCount(); i++)
                        uris.add(clip.getItemAt(i).getUri());
                } else
                    // nur ein Bild ist ausgewählt wurden
                    uris.add(result.getData().getData());

                Uri[] uriArray = uris.toArray(new Uri[0]);

                try{
                    this.loadImages(uriArray); // Load Image = Laden der Daten als Picture Objekte
                    this.showImages(0); // Anzeigen im Frontend
                } catch (PictureSafeBaseException e){
                    // Sollte ein Problem vorliegen, wird Exception Dialog getriggert.
                    PictureSafeDialog.show(getParentFragmentManager(),e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // ViewModel erstellen/laden
        this.mvm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // UI-Komponenten initialisieren
        this.btnSelectPicture = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnSelect), true);
        this.btnReset = new PictureSafeButton(requireContext(), view.findViewById(R.id.btnReset));
        this.infoText = new PictureSafeText(view.findViewById(R.id.infoText), view.findViewById(R.id.infoCard));
        this.thumbnailContainer = view.findViewById(R.id.thumbnailContainer);
        this.imagePreview = new PictureSafeImage(view.findViewById(R.id.mainImage), view.findViewById(R.id.imageCard));

        // OnClick für den Button zum Laden der Bilder
        // erstellt einen Intent, welcher dann in der RegisterForActivityResult verarbeitet wird
        this.btnSelectPicture.button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            this.pickImageLauncher.launch(Intent.createChooser(intent, "Bild auswählen"));
        });

        // Reset button, der Pictures und Filedata wieder zurücksetzt
        this.btnReset.button.setOnClickListener(v -> {
            this.imagePreview.removeImage();
            this.infoText.removeText();

            this.btnReset.changeVisibility(false);

            this.mvm.pictures = null;
            this.mvm.fileData = null;
        });

        // Wenn Bilder vorhanden sind, werden sie angezeigt und der Infotext gesetzt.
        if(this.mvm.pictures != null){
            showImages(this.mvm.selectedPicture);
            this.infoText.setText(PictureUtils.generateInfoText(this.mvm.pictures, this.mvm.selectedPicture));
            this.btnReset.changeVisibility(true);
        }

        return view;
    }

    /** showImages
     *  Zeigt die Bilder im Thumbnail Container an und hebt eines der Bilder als Hauptbild hervor
     *
     * @param selectedPicture Index des fokussierten Bildes
     */
    void showImages(int selectedPicture) {
        this.thumbnailContainer.removeAllViews();

        // Anzeigen der Bilder im Container
        for (int i = 0; i < this.mvm.pictures.length; i++) {
            Bitmap bitmap = this.mvm.pictures[i].bitmap;

            // Generieren der Thumbnails
            ImageView thumb = new ImageView(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(160, 160);
            lp.setMargins(8, 0, 8, 0);
            thumb.setLayoutParams(lp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setImageBitmap(bitmap);

            // OnClickListener um das ausgewählte Bild zu wechseln
            int pictureIndex = i;
            thumb.setOnClickListener(v -> {
                this.imagePreview.setImage(bitmap);
                this.mvm.selectedPicture = pictureIndex;
                this.infoText.setText(PictureUtils.generateInfoText(mvm.pictures, mvm.selectedPicture));
            });

            this.thumbnailContainer.addView(thumb);

            // Ausgewähltes Bild in der Preview anzeigen
            if (i == selectedPicture) {
                this.imagePreview.setImage(bitmap);
            }
        }
    }

    /** loadImages
     *  Laden der Bilder aus den ausgewählten Bildern in den Picture Objekten.
     *
     * @param uris Liste der Bilder
     * @throws IOException Fehler beim Lesen der Bilder
     */
    private void loadImages(Uri[] uris) throws IOException {
        Picture[] pictures = new Picture[uris.length];

        // wichtige Informationen für die Anzeigen (wird im ViewModel gespeichert)
        boolean hasData = false;
        boolean dataIsCorrupted = false;
        DataTypes storedDataType = null;
        boolean usesEncrytion = false;
        String signature = PictureUtils.generateSignature();

        // Bild als Bitmap laden
        for(int i = 0; i < uris.length; i++){
            InputStream inputStream;
            try {
                inputStream = requireContext().getContentResolver().openInputStream(uris[i]);
            } catch (FileNotFoundException e){
                throw new PictureSafeFileNotFoundException();
            }
            Objects.requireNonNull(inputStream);

            // Umwandlung der Bilder über die Bitmap zum Picture-Objekt
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            pictures[i] = new Picture(bitmap, i+1, signature);

            // Sollte das Bild Daten beinhalten, werden die Informationen gespeichert
            if (pictures[i].hasData) {
                hasData = true;
                storedDataType = pictures[i].storedDataType;
                if(!dataIsCorrupted)
                    dataIsCorrupted = pictures[i].dataIsCorrupted;
                if(pictures[i].compressionType.usesEncryption())
                    usesEncrytion = true;
            }
        }

        // Initialisieren der ViewModel Daten
        this.mvm.pictures = pictures;
        this.mvm.picturesLoaded = true;
        this.mvm.picturesAreIncomplete = false;
        this.mvm.picturesHasData = hasData;
        this.mvm.picturesDataIsCorrupted = dataIsCorrupted;
        this.mvm.selectedPicture = 0;
        this.mvm.storedDataType = storedDataType;

        // Sollten Daten vorhanden sein, sollen sie gelesen werden
        if(hasData){
            if(usesEncrytion){
                // Daten sind verschlüsselt -> PasswortDialog wird angezeigt um Passwort abzufragen
                showPasswordDialog(password -> {
                    try {
                        this.readFileData(password);
                    } catch (PictureSafeBaseException e) {
                        PictureSafeDialog.show(getParentFragmentManager(), e);
                    }
                    // Passwort nach nutzung überschreiben
                    Arrays.fill(password, '0');
                });
            }
            else{
                // Daten sind unverschlüsselt vorhanden
                this.readFileData(null);
            }
        }

        this.infoText.setText(PictureUtils.generateInfoText(this.mvm.pictures, this.mvm.selectedPicture));
        this.btnReset.changeVisibility(true);
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
        this.mvm.fileData = new FileData(completeData, mvm.storedDataType, name);
    }

    /** showPasswordDialog
     *  Zeigt ein Dialog an um das Passwort zu eingeben.
     *
     * @param onPassword Passwort zum auslesen der Daten
     */
    private void showPasswordDialog(Consumer<char[]> onPassword) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.password_dialog, null);
        PictureSafeEditText pwEdit = new PictureSafeEditText(view.findViewById(R.id.passwordText), view.findViewById(R.id.passwordCard), true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Verschlüsselte Daten")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Entschlüsseln", (d, w) -> {char[] pw = pwEdit.readText().toCharArray(); onPassword.accept(pw); pwEdit.clearText();})
                .setNegativeButton("Abbrechen", (d, w) -> pwEdit.clearText())
                .show();
    }
}