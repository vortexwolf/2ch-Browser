package ua.in.quireg.chan.boards.makaba;

import java.util.regex.Pattern;

import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaFileInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaFoundPostsList;
import ua.in.quireg.chan.boards.makaba.models.MakabaIconInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaPostInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadsList;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadsListCatalog;
import ua.in.quireg.chan.common.library.MyHtml;
import ua.in.quireg.chan.common.utils.RegexUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.domain.BoardIconModel;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.SearchPostListModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

public class MakabaModelsMapper {

    private static final Pattern sBadgePattern = Pattern.compile("<img.+?src=\"(.+?)\".+?(?:title=\"(.+?)\")?.*?/>");

    public ThreadModel[] mapThreadModels(MakabaThreadsList source) {
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapThreadModel(source.threads[i]);
        }

        return result;
    }

    private ThreadModel mapThreadModel(MakabaThreadInfo source) {
        ThreadModel model = new ThreadModel();
        model.setReplyCount(source.posts.length + source.postsCount);
        int filesCount = 0;
        for (int i = 0; i < source.posts.length; ++i) {
            if (source.posts[i].files != null) {
                filesCount += source.posts[i].files.length;
            }
        }
        filesCount += source.filesCount;
        model.setImageCount(filesCount);
        model.setPosts(this.mapPostModels(source.posts));

        return model;
    }

    public ThreadModel[] mapCatalog(MakabaThreadsListCatalog source) {
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            ThreadModel model = new ThreadModel();
            model.setReplyCount(source.threads[i].postsCount + 1);
            model.setImageCount(source.threads[i].filesCount + source.threads[i].files.length);
            PostModel[] posts = new PostModel[1];
            posts[0] = mapPostModel(source.threads[i]);
            model.setPosts(posts);
            result[i] = model;
        }
        return result;
    }

    public PostModel[] mapPostModels(MakabaPostInfo[] source) {
        PostModel[] result = new PostModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapPostModel(source[i]);
        }

        return result;
    }

    private PostModel mapPostModel(MakabaPostInfo source) {
        PostModel model = new PostModel();
        model.setNumber(source.num);
        model.setName(source.name);
        model.setBadge(parseBadge(source.icon));
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

    public static BoardModel[] mapBoardModels(MakabaBoardInfo[] source) {
        BoardModel[] result = new BoardModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = mapBoardModel(source[i]);
        }
        return result;
    }

    private static BoardModel mapBoardModel(MakabaBoardInfo source) {
        BoardModel model = new BoardModel();

        model.setBumpLimit(Integer.parseInt(source.bump_limit));
        model.setCategory(source.category);
        model.setUserDefaultName(source.default_name);
        model.setEnableLikes(source.enable_likes == 1);
        model.setEnablePosting(source.enable_posting == 1);
        model.setThreadTags(source.enable_thread_tags == 1);
        model.setId(source.id);
        model.setBoardName(source.name);
        model.setPages(source.pages);
        model.setSage(source.sage == 1);
        model.setTripcodes(source.tripcodes == 1);

        model.setIcons(mapBoardIconModels(source.icons));

        return model;
    }

    private static BoardIconModel[] mapBoardIconModels(MakabaIconInfo[] icons) {

        BoardIconModel[] result = new BoardIconModel[icons.length];

        for (int i = 0; i < icons.length; i++) {

            BoardIconModel tempModel = new BoardIconModel();

            tempModel.setIconName(icons[i].name);
            tempModel.setIconNumber(icons[i].num);
            tempModel.setIconUrl(icons[i].url);

            result[i] = tempModel;
        }

        return result;
    }

    private AttachmentModel mapAttachmentModel(MakabaFileInfo file) {
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
