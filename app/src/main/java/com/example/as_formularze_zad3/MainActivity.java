package com.example.as_formularze_zad3;

import android.os.Bundle;
import android.view.View;
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

    private Spinner spCategory;
    private RadioGroup rgPriority;
    private EditText etDescription, etContact;
    private MaterialSwitch swConsent;
    private TextView tvLabelContact, tvSummary;
    private Button btnSubmit, btnReset;

    // Zmienne do zapamiętania stanu formularza (dla funkcji Undo)
    private int savedCategoryPos = 0;
    private int savedPriorityId = -1;
    private String savedDescription = "";
    private boolean savedConsent = false;
    private String savedContact = "";
    private String savedSummary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spCategory = findViewById(R.id.spCategory);
        rgPriority = findViewById(R.id.rgPriority);
        etDescription = findViewById(R.id.etDescription);
        etContact = findViewById(R.id.etContact);
        swConsent = findViewById(R.id.swConsent);
        tvLabelContact = findViewById(R.id.tvLabelContact);
        tvSummary = findViewById(R.id.tvSummary);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnReset = findViewById(R.id.btnReset);

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
        swConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvLabelContact.setVisibility(View.VISIBLE);
                etContact.setVisibility(View.VISIBLE);
            } else {
                tvLabelContact.setVisibility(View.GONE);
                etContact.setVisibility(View.GONE);
                etContact.setText("");
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                askForConfirmation();
            }
        });

        // Obsługa przycisku czyszczenia
        btnReset.setOnClickListener(v -> askForClearConfirmation());
    }

    private boolean validateForm() {
        if (spCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_category, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgPriority.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.error_priority, Toast.LENGTH_SHORT).show();
            return false;
        }

        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etDescription.setError(getString(R.string.error_description));
            etDescription.requestFocus();
            return false;
        }

        if (swConsent.isChecked()) {
            String contact = etContact.getText().toString().trim();
            if (contact.isEmpty()) {
                etContact.setError("Podaj dane kontaktowe");
                etContact.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void askForConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> submitForm())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void submitForm() {
        tvSummary.setText(buildSummary());
        Snackbar.make(findViewById(R.id.main), R.string.sent, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> tvSummary.setText(""))
                .show();
    }

    // Okno dialogowe potwierdzające wyczyszczenie
    private void askForClearConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Wyczyść formularz")
                .setMessage("Czy na pewno chcesz wyczyścić wszystkie pola formularza?")
                .setPositiveButton("Tak", (dialog, which) -> clearFormWithBackup())
                .setNegativeButton("Anuluj", null)
                .show();
    }

    // Zapamiętanie stanu i faktyczne wyczyszczenie formularza
    private void clearFormWithBackup() {
        // 1. Zapisanie obecnego stanu do zmiennych (kopia zapasowa przed skasowaniem)
        savedCategoryPos = spCategory.getSelectedItemPosition();
        savedPriorityId = rgPriority.getCheckedRadioButtonId();
        savedDescription = etDescription.getText().toString();
        savedConsent = swConsent.isChecked();
        savedContact = etContact.getText().toString();
        savedSummary = tvSummary.getText().toString();

        // 2. Wyczyszczenie kontrolek
        spCategory.setSelection(0);
        rgPriority.clearCheck();
        etDescription.setText("");
        swConsent.setChecked(false);
        etContact.setText("");
        tvSummary.setText("");

        // 3. Usunięcie komunikatów błędów z pól
        etDescription.setError(null);
        etContact.setError(null);

        // 4. Pokazanie Snackbar z akcją cofnięcia (Undo)
        Snackbar.make(findViewById(R.id.main), "Formularz został wyczyszczony", Snackbar.LENGTH_LONG)
                .setAction("Cofnij", v -> restoreFormState())
                .show();
    }

    // Przywrócenie stanu formularza z kopii zapasowej po kliknięciu "Cofnij"
    private void restoreFormState() {
        spCategory.setSelection(savedCategoryPos);
        rgPriority.check(savedPriorityId); // jeśli było -1, Android to zignoruje lub wyczyści
        etDescription.setText(savedDescription);
        swConsent.setChecked(savedConsent);
        etContact.setText(savedContact);
        tvSummary.setText(savedSummary);
    }

    private String buildSummary() {
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

        return getString(R.string.summary_format,
                spCategory.getSelectedItem().toString(),
                priorityStr,
                etDescription.getText().toString().trim(),
                contactInfo);
    }
}