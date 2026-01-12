package com.example.picturesafe.components;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.picturesafe.R;
import com.example.picturesafe.enumerators.CompressionDropdown;

/** PictureSafeDropDown
 *  Dropdown für das PictureSafe-Interface mit vordefinierten Funktionen
 */
public class PictureSafeDropDown {
    public Spinner spinner;
    Context context;

    /** PictureSafeDropDown
     *  Konstruktor für die Dropdowns
     *
     * @param context Context der aktuellen Activity
     * @param spinner Dropdown das angezeigt werden soll
     * @param visible ob das Dropdown angezeigt werden soll
     */
    public PictureSafeDropDown(Context context, Spinner spinner, boolean visible) {
        this.context = context;
        this.spinner = spinner;

        changeVisibility(visible);
        setItems();
    }

    /** setItems
     *  Setzt die einzelnen Optionen des Dropdowns
     */
    public void setItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, CompressionDropdown.getAllStrings());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spinner.setAdapter(adapter);
    }

    /** changeVisibility
     *  Ändert die Sichtbarkeit des Dropdowns
     *
     * @param visible ob das Dropdown angezeigt werden soll
     */
    public void changeVisibility(boolean visible) {
        this.spinner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** getSelectedItem
     *  Gibt den ausgewählten Wert des Dropdowns zurück
     *
     * @return aktueller Wert
     */
    public CompressionDropdown getSelectedItem() {
        return CompressionDropdown.fromText(this.spinner.getSelectedItem().toString());
    }
}
