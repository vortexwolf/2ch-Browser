package ua.in.quireg.chan.boards.fourchan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import ua.in.quireg.chan.boards.fourchan.models.FourchanCatalogPage;
import ua.in.quireg.chan.boards.fourchan.models.FourchanCatalogThread;
import ua.in.quireg.chan.boards.fourchan.models.FourchanPostInfo;
import ua.in.quireg.chan.boards.fourchan.models.FourchanThreadInfo;
import ua.in.quireg.chan.boards.fourchan.models.FourchanThreadsList;
import ua.in.quireg.chan.common.library.MyHtml;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

public class FourchanModelsMapper {
    public ThreadModel[] mapThreadModels(FourchanThreadsList source){
        ThreadModel[] result = new ThreadModel[source.threads.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapThreadModel(source.threads[i]);
        }

        return result;
    }

    public ThreadModel mapThreadModel(FourchanThreadInfo source){
        ThreadModel model = new ThreadModel();
        model.setReplyCount(source.posts[0].postsCount);
        model.setImageCount(source.posts[0].filesCount);
        model.setPosts(this.mapPostModels(source.posts));

        return model;
    }


    public PostModel[] mapPostModels(FourchanPostInfo[] source){
        PostModel[] result = new PostModel[source.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapPostModel(source[i]);
        }

        return result;
    }

    public PostModel mapPostModel(FourchanPostInfo source){
        PostModel model = new PostModel();
        model.setNumber(source.number + "");
        model.setName(source.name);
        model.setBadge(this.mapBadge(source.country, source.countryName));
        model.setSubject(MyHtml.fromHtml(StringUtils.emptyIfNull(source.subject)).toString());
        model.setComment(source.comment);
        model.setSage(false);
        model.setTrip(source.trip);
        model.setOp(false);
        if (source.renamedFileName != null) {
            model.addAttachment(this.mapAttachmentModel(source));
        }
        model.setTimestamp(source.timestamp * 1000);
        model.setParentThread(source.parent);

        return model;
    }

    public AttachmentModel mapAttachmentModel(FourchanPostInfo file) {
        AttachmentModel model = new AttachmentModel();
        model.setThumbnailUrl(file.renamedFileName + "s.jpg");
        model.setPath(file.renamedFileName + file.fileExtension);
        model.setImageSize(file.fileSize);
        model.setImageWidth(file.fileWidth);
        model.setImageHeight(file.fileHeight);

        return model;
    }

    public ThreadModel[] mapCatalog(FourchanCatalogPage[] pages) {
        ArrayList<ThreadModel> threads = new ArrayList<ThreadModel>();
        for (FourchanCatalogPage page : pages) {
            threads.addAll(Arrays.asList(this.mapCatalogPage(page)));
        }

        return threads.toArray(new ThreadModel[threads.size()]);
    }

    public ThreadModel[] mapCatalogPage(FourchanCatalogPage page) {
        ThreadModel[] result = new ThreadModel[page.threads.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.mapCatalogThread(page.threads[i]);
        }

        return result;
    }

    public ThreadModel mapCatalogThread(FourchanCatalogThread thread) {
        ThreadModel model = new ThreadModel();
        model.setReplyCount(thread.postsCount);
        model.setImageCount(thread.filesCount);

        PostModel postModel = this.mapPostModel(thread);
        model.setPosts(new PostModel[] { postModel });

        return model;
    }

    private BadgeModel mapBadge(String country, String countryName) {
        if (StringUtils.isEmpty(country)) {
            return null;
        }

        BadgeModel model = new BadgeModel();
        model.source = String.format("country/%s.gif", country.toLowerCase(Locale.US));
        model.title = countryName;
        return model;
    }
}
