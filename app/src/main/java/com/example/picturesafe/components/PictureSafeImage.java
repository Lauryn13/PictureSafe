package com.example.picturesafe.components;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

/** PictureSafeImage
 *  Image für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeImage {
    ImageView imageView;
    CardView cardView;
    boolean constantVisible;

    /** Konstruktor
     *  Konstruktor für die Bilderanzeigen
     *
     * @param imageView ImageView für das Bild
     * @param cardView CardView für das Bild
     * @param constantVisible ob das Bild dauerhaft angezeigt werden soll
     */
    public PictureSafeImage(ImageView imageView, CardView cardView, boolean constantVisible){
        this.imageView = imageView;
        this.cardView = cardView;
        this.constantVisible = constantVisible;

        this.changeVisibility(constantVisible);
    }
    /** PictureSafeImage-Overload
     *  Konstruktor wenn das Bild nicht dauerhaft angezeigt werden soll
     *
     * @param imageView ImageView für das Bild
     * @param cardView CardView für das Bild
     */
    public PictureSafeImage(ImageView imageView, CardView cardView){
        this(imageView, cardView, false);
    }

    /** changeVisibility
     *  Ändert die Sichtbarkeit des Bildes
     *
     * @param visible Sichtbarkeit des Bildes
     */
    public void changeVisibility(boolean visible){
        this.imageView.setVisibility(visible ? ImageView.VISIBLE : ImageView.GONE);
        this.cardView.setVisibility(visible ? CardView.VISIBLE : CardView.GONE);
    }

    /** setImage
     *  Setzt das Bild im ImageView
     *
     * @param bitmap Bitmap des Bildes
     */
    public void setImage(Bitmap bitmap){
        this.changeVisibility(true);
        this.imageView.setImageBitmap(bitmap);
    }

    /** removeImage
     *  Entfernt das Bild aus der ImageView
     */
    public void removeImage(){
        if(!this.constantVisible){
            this.changeVisibility(false);
            this.imageView.setImageBitmap(null);
        }
        else
            this.imageView.setImageBitmap(null);
    }
}
