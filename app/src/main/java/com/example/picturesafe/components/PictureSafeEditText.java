package com.example.picturesafe.components;

import android.widget.EditText;

import androidx.cardview.widget.CardView;

/** PictureSafeEditText
 *  EditText für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeEditText {
    EditText editText;
    CardView cardView;
    boolean constantVisible;

    /** PictureSafeEditText
     *  Konstruktor für die EditTexten
     *
     * @param editText EditText der angezeigt werden soll
     * @param cardView CardView die angezeigt werden soll
     * @param constantVisible ob das Edit dauerhaft angezeigt werden soll
     */
    public PictureSafeEditText(EditText editText, CardView cardView, boolean constantVisible) {
        this.editText = editText;
        this.cardView = cardView;
        this.constantVisible = constantVisible;

        this.changeVisibility(constantVisible);
    }

    /** PictureSafeEditText-Overload
     *  Konstruktor wenn Edit nicht dauerhaft angezeigt werden soll
     *
     * @param editText EditText der angezeigt werden soll
     * @param cardView CardView die angezeigt werden soll
     */
    public PictureSafeEditText(EditText editText, CardView cardView){
        this(editText, cardView, false);
    }

    /** changeVisibility
     *  Ändert die Sichtbarkeit der EditTexten
     *
     * @param visible ob das Edit angezeigt werden soll
     */
    public void changeVisibility(boolean visible){
            this.editText.setVisibility(visible ? EditText.VISIBLE : EditText.GONE);
            this.cardView.setVisibility(visible ? CardView.VISIBLE : CardView.GONE);
    }

    /** readText
     *  Gibt den Inhalt der EditText zurück
     *
     * @return aktueller Inhalt als String
     */
   public String readText() {
       return this.editText.getText().toString();
   }

    /** clearText
     *  Setzt den Text im Edit zurück
     */
   public void clearText(){
        this.editText.setText("");
   }
}
