package ua.in.quireg.chan.test;

import android.test.InstrumentationTestCase;

import ua.in.quireg.chan.boards.makaba.MakabaWebsite;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.presentation.AttachmentInfo;

public class AttachmentInfoTest extends InstrumentationTestCase {

    private final IWebsite mWebsite = new MakabaWebsite();
    private final String mBoardCode = "test";
    private final String mThreadNumber = "123456";

    @Override
    protected void setUp() throws Exception {
        // Instrumentation instr = this.getInstrumentation();
    }

    public void testEmptyAttachment() {
        AttachmentModel model = new AttachmentModel();
        AttachmentInfo info = new AttachmentInfo(model, this.mWebsite, this.mBoardCode, this.mThreadNumber);

        assertTrue(info.isEmpty());
        assertEquals(info.getDescription(), "");
        assertEquals(info.getSourceUrl(), null);
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), null);
        assertEquals(info.getDefaultThumbnail(), ua.in.quireg.chan.R.drawable.page_white_4x);
    }

    public void testNonImageAttachment() {
        AttachmentModel model = new AttachmentModel();
        model.setPath("src/123.mp3");
        model.setImageSize(9000);
        AttachmentInfo info = new AttachmentInfo(model, this.mWebsite, this.mBoardCode, this.mThreadNumber);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription(), "9000KB");
        assertEquals(info.getSourceUrl(), "https://2ch.hk/test/src/123.mp3");
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), "mp3");
        assertEquals(info.getDefaultThumbnail(), ua.in.quireg.chan.R.drawable.page_white_sound_4x);
    }

    public void testImageAttachment() {
        AttachmentModel model = new AttachmentModel();
        model.setPath("src/123.jpg");
        model.setThumbnailUrl("thumb/123s.jpg");
        model.setImageSize(9000);
        AttachmentInfo info = new AttachmentInfo(model, this.mWebsite, this.mBoardCode, this.mThreadNumber);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription(), "9000KB");
        assertEquals(info.getSourceUrl(), "https://2ch.hk/test/src/123.jpg");
        assertEquals(info.getThumbnailUrl(), "https://2ch.hk/test/thumb/123s.jpg");
        assertEquals(info.getSourceExtension(), "jpg");
        assertEquals(info.getDefaultThumbnail(), ua.in.quireg.chan.R.drawable.page_white_4x);
    }
}
