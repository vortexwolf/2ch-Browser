package com.vortexwolf.dvach.activities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.models.presentation.ImageFileModel;
import com.vortexwolf.dvach.models.presentation.SerializableFileModel;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FilesListActivity extends ListActivity {
    public static final String TAG = "FilePickerActivity";

    public final static String EXTRA_CURRENT_FILE = "current_file";
    /** The file path */
    public final static String EXTRA_FILE_PATH = "file_path";

    /** Sets whether hidden files should be visible in the list or not */
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";

    /** The allowed file extensions in an ArrayList of Strings */
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";

    /**
     * The initial directory which will be used if no directory has been sent
     * with the intent
     */
    private final static String SDCARD_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

    protected File mCurrentFile;
    protected boolean mCurrentFileSelected = false;
    protected File mDirectory;
    protected ArrayList<ImageFileModel> mFiles;
    protected FilePickerListAdapter mAdapter;
    protected boolean mShowHiddenFiles = false;
    protected String[] acceptedFileExtensions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication application = (MainApplication) this.getApplication();
        application.getTracker().trackActivityView(TAG);
        ApplicationSettings settings = application.getSettings();

        this.setTheme(settings.getTheme());
        this.setContentView(R.layout.files_list_view);

        mDirectory = new File(SDCARD_DIRECTORY);
        mFiles = new ArrayList<ImageFileModel>();
        mAdapter = new FilePickerListAdapter(this, mFiles);
        setListAdapter(mAdapter);

        // image file extensions
        acceptedFileExtensions = Constants.IMAGE_EXTENSIONS.toArray(new String[Constants.IMAGE_EXTENSIONS.size()]);

        // Get intent extras
        String currentFilePath = null;
        if (getIntent().hasExtra(FilesListActivity.EXTRA_CURRENT_FILE)
                && (currentFilePath = getIntent().getStringExtra(EXTRA_CURRENT_FILE)) != null) {
            mCurrentFile = new File(currentFilePath);
            mDirectory = mCurrentFile.getParentFile();
        } else if (getIntent().hasExtra(EXTRA_FILE_PATH)) {
            String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
            mDirectory = new File(filePath);
        }

        if (getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES)) {
            mShowHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);
        }
        if (getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {
            ArrayList<String> collection = getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);
            acceptedFileExtensions = collection.toArray(new String[collection.size()]);
        }
    }

    @Override
    protected void onResume() {
        refreshFilesList();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        AppearanceUtils.unbindDrawables(this.getListView());
        System.gc();

        super.onDestroy();
    }

    /**
     * Updates the list view to the current directory
     */
    protected void refreshFilesList() {
        // Clear the files ArrayList
        mFiles.clear();

        // Set the extension file filter
        ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);

        // Get the files in the directory
        File[] files = mDirectory.listFiles(filter);
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isHidden() && !mShowHiddenFiles) {
                    // Don't add the file
                    continue;
                }

                // Add the file the ArrayAdapter
                mFiles.add(new ImageFileModel(f));
            }

            Collections.sort(mFiles, new FileComparator());
        }

        if (this.mCurrentFile != null && !this.mCurrentFileSelected) {
            int position = -1;
            for (int i = 0; i < mFiles.size(); i++) {
                if (mFiles.get(i).file.getAbsolutePath().equals(this.mCurrentFile.getAbsolutePath())) {
                    position = i;
                    break;
                }
            }

            this.getListView().setSelection(position);
            this.mCurrentFileSelected = true;
        }

        this.setTitle(mDirectory.getAbsolutePath());
        mAdapter.notifyDataSetChanged();

    }

    // for backward compatibility
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDirectory.getParentFile() != null
                    && !mDirectory.getPath().endsWith(SDCARD_DIRECTORY)) {
                // Go to parent directory
                mDirectory = mDirectory.getParentFile();
                refreshFilesList();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ImageFileModel newFile = (ImageFileModel) l.getItemAtPosition(position);

        if (newFile.file.isFile()) {
            // Set result
            Intent extra = new Intent();
            extra.putExtra(getPackageName() + Constants.EXTRA_SELECTED_FILE, new SerializableFileModel(newFile));
            setResult(RESULT_OK, extra);
            // Finish the activity
            finish();
        } else {
            mDirectory = newFile.file;
            // Update the files list
            refreshFilesList();
        }

        super.onListItemClick(l, v, position, id);
    }

    private class FilePickerListAdapter extends ArrayAdapter<ImageFileModel> {

        private List<ImageFileModel> mObjects;
        private final LayoutInflater mInflater;

        public FilePickerListAdapter(Context context, List<ImageFileModel> objects) {
            super(context, 0, objects);
            mObjects = objects;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                view = this.mInflater.inflate(R.layout.files_list_item, parent, false);
            } else {
                view = convertView;
            }

            ImageFileModel object = mObjects.get(position);

            ImageView frame = (ImageView) view.findViewById(R.id.filelist_thumb_frame);
            ImageView imageView = (ImageView) view.findViewById(R.id.filelist_icon);
            TextView textView = (TextView) view.findViewById(R.id.file_picker_text);
            TextView statusView = (TextView) view.findViewById(R.id.filelist_status);

            textView.setText(object.file.getName());

            if (object.file.isFile()) {
                // Show the file icon
                // imageView.setImageResource(R.drawable.file);
                // String filePath = object.getAbsolutePath();
                // Uri fileUri = Uri.parse(filePath);

                String statusText = (object.file.length() / 1024) + "Kb";

                ImageFileModel bitmapModel = new ImageFileModel(object.file);
                if (bitmapModel.getBitmap() != null) {
                    imageView.setImageBitmap(bitmapModel.getBitmap());
                    frame.setVisibility(View.VISIBLE);
                    statusText += ", " + bitmapModel.imageWidth + "x"
                            + bitmapModel.imageHeight;
                } else {
                    MyLog.v(TAG, "Bitmap is null");
                    imageView.setImageResource(android.R.color.transparent);
                    frame.setVisibility(View.INVISIBLE);
                }

                statusView.setVisibility(View.VISIBLE);
                statusView.setText(statusText);

            } else {
                // Show the folder icon
                frame.setVisibility(View.INVISIBLE);

                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setImageResource(R.drawable.folder);
                statusView.setVisibility(View.GONE);
            }

            return view;
        }
    }

    private class FileComparator implements Comparator<ImageFileModel> {
        @Override
        public int compare(ImageFileModel f1, ImageFileModel f2) {
            if (f1.file == f2.file) {
                return 0;
            }
            if (f1.file.isDirectory() && f2.file.isFile()) {
                // Show directories above files
                return -1;
            }
            if (f1.file.isFile() && f2.file.isDirectory()) {
                // Show files below directories
                return 1;
            }
            // Sort the directories alphabetically
            return f1.file.getName().compareToIgnoreCase(f2.file.getName());
        }
    }

    private class ExtensionFilenameFilter implements FilenameFilter {
        private String[] mExtensions;

        public ExtensionFilenameFilter(String[] extensions) {
            super();
            mExtensions = extensions;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (new File(dir, filename).isDirectory()) {
                // Accept all directory names
                return true;
            }
            if (mExtensions != null && mExtensions.length > 0) {
                for (int i = 0; i < mExtensions.length; i++) {
                    if (filename.endsWith(mExtensions[i])) {
                        // The filename ends with the extension
                        return true;
                    }
                }
                // The filename did not match any of the extensions
                return false;
            }
            // No extensions has been set. Accept all file extensions.
            return true;
        }
    }
}
