package ua.in.quireg.chan.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ua.in.quireg.chan.db.dao.MakabaDao;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.domain.BoardIconModel;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

@Database(entities = {
        BoardModel.class,
        BoardIconModel.class,
        PostModel.class,
        AttachmentModel.class,
        ThreadModel.class,
        BadgeModel.class},version = 1, exportSchema = false)
public abstract class DvachRoomDatabase extends RoomDatabase {

    public abstract MakabaDao makabaDao();

    private static DvachRoomDatabase INSTANCE;

    public static DvachRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DvachRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DvachRoomDatabase.class, "dvach_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
