package com.vortexwolf.chan.test;

import android.app.Instrumentation;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.test.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AttachmentInfoTest extends InstrumentationTestCase {

    private final String mBoardCode = "test";
    private final String mThreadNumber = "123456";

    @Override
    protected void setUp() throws Exception {
        // Instrumentation instr = this.getInstrumentation();
    }

    public void testEmptyAttachment() {
        AttachmentModel model = new AttachmentModel();
        AttachmentInfo info = new AttachmentInfo(model, this.mBoardCode, this.mThreadNumber);

        assertTrue(info.isEmpty());
        assertEquals(info.getDescription(), "");
        assertEquals(info.getSourceUrl(), null);
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), null);
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_4x);
    }

    public void testNonImageAttachment() {
        AttachmentModel model = new AttachmentModel();
        model.setPath("src/123.mp3");
        model.setImageSize(9000);
        AttachmentInfo info = new AttachmentInfo(model, this.mBoardCode, this.mThreadNumber);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription(), "9000KB");
        assertEquals(info.getSourceUrl(), "https://2ch.hk/test/src/123.mp3");
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), "mp3");
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_sound_4x);
    }

    public void testImageAttachment() {
        AttachmentModel model = new AttachmentModel();
        model.setPath("src/123.jpg");
        model.setThumbnailUrl("thumb/123s.jpg");
        model.setImageSize(9000);
        AttachmentInfo info = new AttachmentInfo(model, this.mBoardCode, this.mThreadNumber);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription(), "9000KB");
        assertEquals(info.getSourceUrl(), "https://2ch.hk/test/src/123.jpg");
        assertEquals(info.getThumbnailUrl(), "https://2ch.hk/test/thumb/123s.jpg");
        assertEquals(info.getSourceExtension(), "jpg");
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_4x);
    }
}
