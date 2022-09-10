package com.vortexwolf.chan.boards.makaba;

import java.util.regex.Pattern;

import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaFileInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaFoundPostsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaPostInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsListCatalog;
import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.models.domain.BadgeModel;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;

public class MakabaModelsMapper {
    private static final Pattern sBadgePattern = Pattern.compile("<img.+?src=\"(.+?)\".+?(?:title=\"(.+?)\")?.*?/>");

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

    public ThreadModel[] mapCatalog(MakabaThreadsListCatalog source){
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            ThreadModel model = new ThreadModel();
            model.setReplyCount(source.threads[i].postsCount+1);
            model.setImageCount(source.threads[i].filesCount+source.threads[i].files.length);
            PostModel[] posts = new PostModel[1];
            posts[0]=mapPostModel(source.threads[i]);
            model.setPosts(posts);
            result[i] = model;
        }
        return result;
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
        model.setBadge(this.parseBadge(source.icon));
        model.setSubject(MyHtml.fromHtml(StringUtils.emptyIfNull(source.subject)).toString());
        model.setComment(source.comment);
        model.setSage("mailto:sage".equals(source.email));
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

    public static BoardModel mapBoardModel(MakabaBoardInfo source){
        BoardModel model = new BoardModel();

        model.setBump_limit(String.valueOf(source.bump_limit));
        model.setCategory(source.category);
        model.setDefault_name(source.default_name);
        model.setEnable_likes(source.enable_likes ? 1 : 0);
        model.setEnable_posting(source.enable_posting ? 1 : 0);
        model.setEnable_thread_tags(source.enable_thread_tags ? 1 : 0);
        model.setId(source.id);
        model.setName(source.name);
        model.setPages(source.pages);
        model.setSage(source.sage ? 1 : 0);
        model.setTripcodes(source.tripcodes ? 1 : 0);
        model.setIcons(source.icons);

        return model;
    }

    public static BoardModel[] mapBoardModels(MakabaBoardInfo[] source){
        BoardModel[] result = new BoardModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = mapBoardModel(source[i]);
        }
        return result;
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

    private BadgeModel parseBadge(String icon) {
        if (StringUtils.isEmpty(icon)) {
            return null;
        }

        String[] imgGroups = RegexUtils.getGroupValues(icon, sBadgePattern);
        if (imgGroups == null) {
            return null;
        }

        BadgeModel model = new BadgeModel();
        model.source = imgGroups[1];
        model.title = imgGroups[2];
        return model;
    }
}
