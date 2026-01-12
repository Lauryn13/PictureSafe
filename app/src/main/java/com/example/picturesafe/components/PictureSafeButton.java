package com.example.picturesafe.components;

import android.content.Context;
import android.widget.Button;
import android.view.View;

/** PictureSafeButton
 *  Button für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeButton{
    public View button;
    Context context;

    /** PictureSafeButton
     *  Konstruktor für die Buttons
     *
     * @param context Context der aktuellen Activity
     * @param button Button der angezeigt werden soll
     * @param visible ob der Button angezeigt werden soll
     */
    public PictureSafeButton(Context context, View button, boolean visible){
        super();
        this.button = button;
        this.context = context;

        this.button.setVisibility(visible ? Button.VISIBLE : Button.GONE);
    }

    /** PictureSafeButton-Overload
     *  Konstruktor wenn Buttons nicht sichtbar sein sollen von Anfang an
     *
     * @param context Context der aktuellen Activity
     * @param button Button, welcher angezeigt werden soll
     */
    public PictureSafeButton(Context context, View button){
        this(context, button, false);
    }

    /** changeVisibility
     *  Ändert die Sichtbarkeit des Buttons
     *
     * @param visible ob der Button angezeigt werden soll
     */
    public void changeVisibility(boolean visible){
        if(visible)
            this.button.setVisibility(Button.VISIBLE);
        else
            this.button.setVisibility(Button.GONE);
    }

    /** changeText
     *  Ändert den Text des Buttons
     *
     * @param text neuer Text
     */
    public void changeText(String text){
        ((Button)this.button).setText(text);
    }
}
