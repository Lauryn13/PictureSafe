package com.example.picturesafe.components;

import android.widget.TextView;

import androidx.cardview.widget.CardView;

/** PictureSafeText
 *  Text für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeText {
    public TextView textView;
    public CardView cardView;

    /** Konstruktor
     *  Konstruktor für die Textanzeigen
     *
     * @param textView TextView die angezeigt werden soll
     * @param cardView CardView die angezeigt werden soll
     * @param visible ob der Text angezeigt werden soll
     */
    public PictureSafeText(TextView textView, CardView cardView, boolean visible) {
        this.textView = textView;
        this.cardView = cardView;

        this.textView.setVisibility(visible ? TextView.VISIBLE : TextView.GONE);
        this.cardView.setVisibility(visible ? CardView.VISIBLE : CardView.GONE);
    }

    /** Konstruktor-Overload
     *  Konstruktor wenn der Text nicht dauerhaft angezeigt werden soll
     *
     * @param textView TextView die angezeigt werden soll
     * @param cardView CardView die angezeigt werden soll
     */
    public PictureSafeText(TextView textView, CardView cardView){
        this(textView, cardView, false);
    }

    /** setText
     *  Setzt den Text
     *
     * @param text neuer Text
     */
    public void setText(String text) {
        textView.setVisibility(TextView.VISIBLE);
        textView.setText(text);
        cardView.setVisibility(CardView.VISIBLE);
    }

    /** removeText
     *  Entfernt den Text
     */
    public void removeText(){
        textView.setVisibility(TextView.GONE);
        textView.setText("");
        cardView.setVisibility(CardView.VISIBLE);
    }
}
