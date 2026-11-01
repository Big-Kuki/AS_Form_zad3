package com.example.as_formularze_zad3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    // Deklaracja kontrolek
    private Spinner spCategory;
    private RadioGroup rgPriority;
    private EditText etDescription, etContact;
    private MaterialSwitch swConsent;
    private TextView tvLabelContact, tvSummary;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Przypisanie widoków po ID
        spCategory = findViewById(R.id.spCategory);
        rgPriority = findViewById(R.id.rgPriority);
        etDescription = findViewById(R.id.etDescription);
        etContact = findViewById(R.id.etContact);
        swConsent = findViewById(R.id.swConsent);
        tvLabelContact = findViewById(R.id.tvLabelContact);
        tvSummary = findViewById(R.id.tvSummary);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Konfiguracja Spinnera, nasłuchiwaczy i stanu początkowego
        setUpSpinner();
        setUpListeners();
    }

    private void setUpSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.defect_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void setUpListeners() {
        // ZALEŻNOŚĆ MIĘDZY POLAMI:
        // Gdy przełącznik zgody na kontakt jest włączony, pokazujemy pole kontaktowe.
        // Gdy wyłączony – ukrywamy je i czyszczymy.
        swConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvLabelContact.setVisibility(View.VISIBLE);
                etContact.setVisibility(View.VISIBLE);
            } else {
                tvLabelContact.setVisibility(View.GONE);
                etContact.setVisibility(View.GONE);
                etContact.setText(""); // czyszczenie pola
            }
        });

        // Kliknięcie przycisku wysyłania uruchamia walidację
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                askForConfirmation();
            }
        });
    }

    private boolean validateForm() {
        // 1. Walidacja Spinnera (pozycja 0 to "-- wybierz kategorię --")
        if (spCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_category, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Walidacja RadioGroup (czy zaznaczono priorytet)
        if (rgPriority.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.error_priority, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 3. Walidacja opisu usterki (EditText)
        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etDescription.setError(getString(R.string.error_description));
            etDescription.requestFocus();
            return false;
        }

        // 4. Walidacja zależności (jeśli zgoda jest zaznaczona, kontakt nie może być pusty)
        if (swConsent.isChecked()) {
            String contact = etContact.getText().toString().trim();
            if (contact.isEmpty()) {
                etContact.setError("Podaj dane kontaktowe");
                etContact.requestFocus();
                return false;
            }
        }

        return true; // Wszystkie pola poprawne!
    }

    private void askForConfirmation() {
        // Okno dialogowe pytające o potwierdzenie
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> submitForm())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void submitForm() {
        // Wyświetlenie podsumowania i paska Snackbar z opcją cofnięcia
        tvSummary.setText(buildSummary());

        Snackbar.make(findViewById(R.id.main), R.string.sent, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> tvSummary.setText(""))
                .show();
    }

    private String buildSummary() {
        // Odczyt wybranego priorytetu z RadioGroup
        int priorityId = rgPriority.getCheckedRadioButtonId();
        String priorityStr = "";
        if (priorityId == R.id.rbLow) {
            priorityStr = getString(R.string.priority_low);
        } else if (priorityId == R.id.rbMedium) {
            priorityStr = getString(R.string.priority_medium);
        } else if (priorityId == R.id.rbHigh) {
            priorityStr = getString(R.string.priority_high);
        }

        String contactInfo = swConsent.isChecked() ? etContact.getText().toString().trim() : "brak";

        // Składanie końcowego tekstu podsumowania
        return getString(R.string.summary_format,
                spCategory.getSelectedItem().toString(),
                priorityStr,
                etDescription.getText().toString().trim(),
                contactInfo);
    }
}