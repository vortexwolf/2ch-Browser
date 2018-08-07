package ua.in.quireg.chan.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.domain.BoardIconModel;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

@Dao
public interface MakabaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AttachmentModel attachmentModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PostModel postModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ThreadModel threadModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BoardIconModel boardIconModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BoardModel boardModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BadgeModel badgeModel);


//    @Query("DELETE FROM word_table")
//    void deleteAll();

    @Query("SELECT * FROM attachmentModel WHERE thumbnailUrl = :thumbnailUrl")
    AttachmentModel getAttachmentByUrl(String thumbnailUrl);

    @Query("SELECT * FROM attachmentModel WHERE postId = :postId")
    List<AttachmentModel> getAttachmentsForPost(String postId);

    @Query("SELECT * FROM postModel WHERE parentThread = :parentThread")
    List<PostModel> getPostModelsForThread(String parentThread);

    @Query("SELECT * FROM threadModel WHERE board = :board")
    List<ThreadModel> getThreadModelsForBoard(String board);
}
