package com.example.picturesafe.components;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/** PictureSafeCheckBox
 *  Checkbox für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeCheckBox {
    public CheckBox checkBox;
    public TextView text;

    /** PictureSafeCheckBox
     *  Konstruktor für die Checkboxen
     *
     * @param text TextView der angezeigt werden soll
     * @param checkBox CheckBox der angezeigt werden soll
     * @param visible ob die Checkbox angezeigt werden soll
     */
    public PictureSafeCheckBox(TextView text, CheckBox checkBox, boolean visible) {
        this.checkBox = checkBox;
        this.text = text;

        changeVisibility(visible);
    }

    /** changeVisibilty
     *  Ändert die Sichtbarkeit der Checkboxen
     *
     * @param visible ob die Checkbox angezeigt werden soll
     */
    public void changeVisibility(boolean visible) {
        this.checkBox.setVisibility(visible ? View.VISIBLE : View.GONE);
        this.text.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** isChecked
     *  Gibt den Status der Checkbox zurück
     *
     * @return ist die Checkbox ausgewählt
     */
    public boolean isChecked() {
        return checkBox.isChecked();
    }
}
