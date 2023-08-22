package com.android.factorytest.utils;

import android.content.Context;
import android.os.Environment;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class DialogUtils {
    public interface DialogSelection {
        void onSelectedFilePaths(String[] files);
    }

    public static void selectFile(Context context, final DialogSelection dialogSelection) {
        String default_dir = "/storage";//Environment.getExternalStorageDirectory().getAbsolutePath();
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(default_dir);
        properties.error_dir = new File(default_dir);
        properties.offset = new File(default_dir);
//        properties.extensions = new String[]{"zip"};
        FilePickerDialog dialog = new FilePickerDialog(context, properties);
        dialog.setTitle("请选择文件");
        dialog.show();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                dialogSelection.onSelectedFilePaths(files);
            }
        });
    }
}
