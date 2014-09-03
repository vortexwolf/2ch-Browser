package com.vortexwolf.chan.boards.makaba;

import com.vortexwolf.chan.boards.makaba.models.MakabaFileInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaFoundPostsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaPostInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsList;
import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;

public class MakabaModelsMapper {

    public ThreadModel[] mapThreadModels(MakabaThreadsList source){
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapThreadModel(source.threads[i]);
        }
        
        return result;
    }
    
    public ThreadModel mapThreadModel(MakabaThreadInfo source){
        ThreadModel model = new ThreadModel();
        model.setReplyCount(source.posts.length + source.postsCount);
        int filesCount = 0;
        for (int i=0; i<source.posts.length; ++i) {
            if (source.posts[i].files != null) {
                filesCount += source.posts[i].files.length;
            }
        }
        filesCount += source.filesCount;
        model.setImageCount(filesCount);
        model.setPosts(this.mapPostModels(source.posts));
        
        return model;
    }
    
    public PostModel[] mapPostModels(MakabaPostInfo[] source){
        PostModel[] result = new PostModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapPostModel(source[i]);
        }
        
        return result;
    }
    
    public PostModel mapPostModel(MakabaPostInfo source){
        PostModel model = new PostModel();
        model.setNumber(source.num);
        model.setName(source.name);
        model.setSubject(MyHtml.fromHtml(StringUtils.emptyIfNull(source.subject)).toString());
        model.setComment(source.comment);
        model.setEmail(source.email);
        model.setTrip(source.trip);
        model.setOp(source.op == 1);
        if (source.files != null) {
            for (MakabaFileInfo file : source.files) {
                model.addAttachment(this.mapAttachmentModel(file));
            }
        }
        model.setTimestamp(source.timestamp != 0 ? source.timestamp * 1000 : ThreadPostUtils.parseMoscowTextDate(source.date));
        model.setParentThread(source.parent);
        
        return model;
    }
    
    public AttachmentModel mapAttachmentModel(MakabaFileInfo file) {
        AttachmentModel model = new AttachmentModel();
        model.setThumbnailUrl(file.thumbnail);
        model.setPath(file.path);
        model.setImageSize(file.size);
        model.setImageWidth(file.width);
        model.setImageHeight(file.height);
        
        return model;
    }
    
    public SearchPostListModel mapSearchPostListModel(MakabaFoundPostsList source) {
        SearchPostListModel model = new SearchPostListModel();
        model.setPosts(this.mapPostModels(source.posts));
        
        return model;
    }
}
