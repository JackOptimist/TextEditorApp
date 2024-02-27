package com.example.texteditorapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int SAVE_FILE_REQUEST_CODE = 1;
    private static final int OPEN_FILE_REQUEST_CODE = 2;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);

        editText.setOnLongClickListener(v -> {
            registerForContextMenu(editText);
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.color) {
            showColorPickerDialog();
            return true;
        } else if (id == R.id.align) {
            showAlignmentDialog();
            return true;
        } else if (id == R.id.style) {
            showStyleDialog();
            return true;
        } else if (id == R.id.size) {
            showFontSizeDialog();
            return true;
        } else if (id == R.id.cut) {
            cutText();
            return true;
        } else if (id == R.id.copy) {
            copyText();
            return true;
        } else if (id == R.id.paste) {
            pasteText();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.open_action) {
            openFile();
            return true;
        } else if (id == R.id.save_action) {
            saveFile();
            return true;
        } else if (id == R.id.save_as_action) {
            saveFileAs();
            return true;
        } else if (id == R.id.exit_action) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showColorPickerDialog() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        final int[] colors = {
                Color.BLACK,
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
                Color.GRAY,
                Color.DKGRAY,
                Color.LTGRAY
        };

        final String[] colorNames = {
                "Black",
                "Red",
                "Green",
                "Blue",
                "Yellow",
                "Cyan",
                "Magenta",
                "Gray",
                "Dark-Gray",
                "Light-Gray"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose color of text:");

        builder.setItems(colorNames, (dialog, which) -> editText.getText().setSpan(new ForegroundColorSpan(colors[which]), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        builder.show();
    }

    private void showAlignmentDialog() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose alignment");
        String[] alignments = {"Left", "Center", "Right"};
        builder.setItems(alignments, (dialog, which) -> {
            Layout.Alignment alignment;
            switch (which) {
                case 1:
                    alignment = Layout.Alignment.ALIGN_CENTER;
                    break;
                case 2:
                    alignment = Layout.Alignment.ALIGN_OPPOSITE;
                    break;
                case 0:
                default:
                    alignment = Layout.Alignment.ALIGN_NORMAL;
                    break;
            }
            editText.getText().setSpan(new AlignmentSpan.Standard(alignment), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        });
        builder.show();
    }

    private void showStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose text style");

        String[] styles = {"Bold", "Italic", "Underline"};
        builder.setItems(styles, (dialog, which) -> {
            switch (which) {
                case 0:
                    toggleBold();
                    break;
                case 1:
                    toggleItalic();
                    break;
                case 2:
                    toggleUnderline();
                    break;
            }
        });

        builder.show();
    }

    private void toggleBold() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        Spannable spannable = editText.getText();
        StyleSpan[] styleSpans = spannable.getSpans(startSelection, endSelection, StyleSpan.class);

        boolean isBold = false;
        for (StyleSpan styleSpan : styleSpans) {
            if (styleSpan.getStyle() == Typeface.BOLD) {
                isBold = true;
                break;
            }
        }

        if (isBold) {
            spannable.removeSpan(new StyleSpan(Typeface.BOLD));
        } else {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleItalic() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        Spannable spannable = editText.getText();
        StyleSpan[] styleSpans = spannable.getSpans(startSelection, endSelection, StyleSpan.class);

        boolean isItalic = false;
        for (StyleSpan styleSpan : styleSpans) {
            if (styleSpan.getStyle() == Typeface.ITALIC) {
                isItalic = true;
                break;
            }
        }

        if (isItalic) {
            spannable.removeSpan(new StyleSpan(Typeface.ITALIC));
        } else {
            spannable.setSpan(new StyleSpan(Typeface.ITALIC), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleUnderline() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        Spannable spannable = editText.getText();
        UnderlineSpan[] underlineSpans = spannable.getSpans(startSelection, endSelection, UnderlineSpan.class);

        boolean isUnderlined = false;
        for (UnderlineSpan ignored : underlineSpans) {
            isUnderlined = true;
            break;
        }

        if (isUnderlined) {
            spannable.removeSpan(new UnderlineSpan());
        } else {
            spannable.setSpan(new UnderlineSpan(), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void showFontSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose font size");

        final float[] fontSizes = {0.5f, 0.7f, 0.85f, 1.0f, 1.15f, 1.3f, 1.5f, 1.7f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f};
        final String[] fontSizeNames = new String[fontSizes.length];
        for (int i = 0; i < fontSizes.length; i++) {
            fontSizeNames[i] = String.valueOf((int)(fontSizes[i] * 12));
        }

        builder.setItems(fontSizeNames, (dialog, which) -> {
            int startSelection = editText.getSelectionStart();
            int endSelection = editText.getSelectionEnd();

            if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
                return;
            }

            editText.getText().setSpan(new RelativeSizeSpan(fontSizes[which]), startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        });

        builder.show();
    }

    private void cutText() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        copyText();

        editText.getText().replace(startSelection, endSelection, "");
    }

    private void copyText() {
        int startSelection = editText.getSelectionStart();
        int endSelection = editText.getSelectionEnd();

        if (startSelection == -1 || endSelection == -1 || startSelection == endSelection) {
            return;
        }

        String selectedText = editText.getText().toString().substring(startSelection, endSelection);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", selectedText);
        clipboardManager.setPrimaryClip(clipData);
    }

    private void pasteText() {
        int cursorPosition = editText.getSelectionStart();

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager.hasPrimaryClip()) {
            ClipData.Item item = Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0);
            String clipboardText = item.getText().toString();

            editText.getText().insert(cursorPosition, clipboardText);
        }
    }

    private void saveFile() {
        String textToSave = editText.getText().toString();

        try {
            File file = new File(getFilesDir(), "saved_file.txt");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(textToSave.getBytes());
            fos.close();

            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFileAs() {
        String textToSave = editText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "saved_file.txt");

        startActivityForResult(intent, SAVE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SAVE_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                saveFileToUri(uri);
            } else {
                Toast.makeText(this, "File save cancelled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == OPEN_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                openFileFromUri(uri);
            } else {
                Toast.makeText(this, "File open cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveFileToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                String textToSave = editText.getText().toString();
                outputStream.write(textToSave.getBytes());
                outputStream.close();
                Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
    }

    private void openFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();
                editText.setText(stringBuilder.toString());
                Toast.makeText(this, "File opened successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }
}
