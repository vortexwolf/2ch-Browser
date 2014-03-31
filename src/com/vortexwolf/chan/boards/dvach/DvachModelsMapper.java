package com.vortexwolf.chan.boards.dvach;

import com.vortexwolf.chan.boards.dvach.models.DvachFoundPostsList;
import com.vortexwolf.chan.boards.dvach.models.DvachPostInfo;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadInfo;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadsList;
import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;

public class DvachModelsMapper {
    
    public ThreadModel[] mapThreadModels(DvachThreadsList source){
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapThreadModel(source.threads[i]);
        }
        
        return result;
    }
    
    public ThreadModel mapThreadModel(DvachThreadInfo source){
        ThreadModel model = new ThreadModel();
        model.setReplyCount(source.replyCount);
        model.setImageCount(source.imageCount);
        model.setPosts(this.mapPostModels(source.posts[0]));
        
        return model;
    }
    
    public PostModel[] mapPostModels(DvachPostInfo[] source){
        PostModel[] result = new PostModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapPostModel(source[i]);
        }
        
        return result;
    }
    
    public PostModel mapPostModel(DvachPostInfo source){
        PostModel model = new PostModel();
        model.setNumber(source.num);
        model.setName(source.name != null ? source.name : source.postername);
        model.setSubject(MyHtml.fromHtml(StringUtils.emptyIfNull(source.subject)).toString());
        model.setComment(source.comment);
        model.setThumbnailUrl(source.thumbnail);
        model.setVideoUrl(source.video);
        model.setImageUrl(source.image);
        model.setImageSize(source.size);
        model.setImageWidth(source.width);
        model.setImageHeight(source.height);
        model.setTimestamp(source.timestamp != 0 ? source.timestamp * 1000 : ThreadPostUtils.parseMoscowTextDate(source.date));
        model.setParentThread(source.parent);
        
        return model;
    }
    
    public SearchPostListModel mapSearchPostListModel(DvachFoundPostsList source) {
        SearchPostListModel model = new SearchPostListModel();
        model.setPosts(this.mapPostModels(source.posts));
        model.setError(source.errorText);
        
        return model;
    }
}
