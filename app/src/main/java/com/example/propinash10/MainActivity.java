package com.example.propinash10;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnVerTotal;
    private TextView txtTotalPropinas;
    private SharedPreferences sharedPreferences;
    private String selectedWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FORZAR EL IDIOMA A ESPAÑOL
        Locale locale = new Locale("es");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        // Vincular elementos del layout
        calendarView = findViewById(R.id.calendarView);
        btnVerTotal = findViewById(R.id.btnVerTotal);
        txtTotalPropinas = findViewById(R.id.txtTotalPropinas);
        sharedPreferences = getSharedPreferences("Propinas", Context.MODE_PRIVATE);

        // Evento al seleccionar un día en el calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedWeek = getWeekOfYear(calendar);
            showInputDialog(); // Abre el diálogo de ingreso de propinas
        });

        // Evento para ver el total de propinas
        btnVerTotal.setOnClickListener(view -> mostrarTotalPropinas());
    }

    // Método para obtener la semana del año
    private String getWeekOfYear(Calendar calendar) {
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        return "Semana" + week;
    }

    // Método para mostrar el cuadro de diálogo de ingreso de propina
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingrese propina para la " + selectedWeek);

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Cargar el valor existente si ya hay una propina guardada
        input.setText(getPropina(selectedWeek));

        builder.setView(input);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String propina = input.getText().toString().trim();
            if (!propina.isEmpty()) {
                try {
                    Float.parseFloat(propina); // Validar que es un número válido
                    savePropina(selectedWeek, propina);
                    Toast.makeText(this, "Propina guardada.", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Ingrese un valor numérico válido.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Ingrese un valor válido.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Método para guardar la propina en SharedPreferences
    private void savePropina(String week, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(week, value);
        editor.apply();
    }

    // Método para obtener la propina guardada
    private String getPropina(String week) {
        return sharedPreferences.getString(week, "0.0");
    }

    // Método para calcular el total de todas las propinas guardadas
    private void mostrarTotalPropinas() {
        float total = 0.0f;

        if (sharedPreferences.getAll().isEmpty()) {
            txtTotalPropinas.setText("Total: 0€");
            return;
        }

        for (String key : sharedPreferences.getAll().keySet()) {
            try {
                // Verificamos si el valor guardado es Float o String y lo convertimos correctamente
                Object valor = sharedPreferences.getAll().get(key);

                if (valor instanceof Float) {
                    total += (Float) valor;
                } else if (valor instanceof String) {
                    total += Float.parseFloat((String) valor);
                }

            } catch (NumberFormatException | ClassCastException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error en los datos guardados. Restableciendo valores.", Toast.LENGTH_LONG).show();

                // Eliminar valores corruptos y seguir
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(key);
                editor.apply();
            }
        }

        txtTotalPropinas.setText(String.format(Locale.getDefault(), "Total: %.2f€", total));
    }
}
