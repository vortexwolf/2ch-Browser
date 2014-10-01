package com.vortexwolf.chan.models.presentation;

import java.io.File;

import android.os.Parcel;
import android.os.Parcelable;

public class SerializableFileModel extends ImageFileModel implements Parcelable {
    public SerializableFileModel() {

    }

    public SerializableFileModel(ImageFileModel copy) {
        this.file = copy.file;
        this.imageHeight = copy.imageHeight;
        this.imageWidth = copy.imageWidth;
    }

    public SerializableFileModel(Parcel in) {
        this.readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.file.getAbsolutePath());
        // dest.writeParcelable(bitmap, flags);

        dest.writeInt(this.imageHeight);
        dest.writeInt(this.imageWidth);
    }

    private void readFromParcel(Parcel in) {
        this.file = new File(in.readString());
        // bitmap = in.readParcelable(Bitmap.class.getClassLoader());

        this.imageHeight = in.readInt();
        this.imageWidth = in.readInt();
    }

    public static final Parcelable.Creator<SerializableFileModel> CREATOR = new Parcelable.Creator<SerializableFileModel>() {
        @Override
        public SerializableFileModel createFromParcel(Parcel in) {
            return new SerializableFileModel(in);
        }

        @Override
        public SerializableFileModel[] newArray(int size) {
            return new SerializableFileModel[size];
        }
    };
}