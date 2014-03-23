package com.vortexwolf.chan.test;

import android.app.Instrumentation;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.test.R;
import com.vortexwolf.chan.test.mocks.MockAttachmentEntity;
import com.vortexwolf.chan.models.domain.IAttachmentEntity;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.services.presentation.DvachUriBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AttachmentInfoTest extends InstrumentationTestCase {

    private final String mBoardCode = "test";
    private ApplicationSettings mSettings;
    private DvachUriBuilder mDvachUriBuilder;

    @Override
    protected void setUp() throws Exception {
        Instrumentation instr = this.getInstrumentation();
        this.mSettings = new ApplicationSettings(instr.getContext(), instr.getContext().getResources());
        this.mDvachUriBuilder = new DvachUriBuilder(Uri.parse("http://2ch.hk"));
    }

    public void testEmptyAttachment() {
        IAttachmentEntity entity = new MockAttachmentEntity("", "", 0, "");
        AttachmentInfo info = new AttachmentInfo(entity, this.mBoardCode, this.mDvachUriBuilder);

        assertTrue(info.isEmpty());
        assertEquals(info.getDescription(""), "");
        assertEquals(info.getSourceUrl(this.mSettings), null);
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), null);
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_4x);
    }

    public void testVideoAttachment() {
        String videoHtml = "<object width=\"320\" height=\"262\"><param name=\"movie\" value=\"http://www.youtube.com/v/dQw4w9WgXcQ\"></param><param name=\"wmode\" value=\"transparent\"></param><embed src=\"http://www.youtube.com/v/dQw4w9WgXcQ\" type=\"application/x-shockwave-flash\" wmode=\"transparent\" width=\"320\" height=\"262\"></embed></object>";
        IAttachmentEntity entity = new MockAttachmentEntity(null, null, 0, videoHtml);
        AttachmentInfo info = new AttachmentInfo(entity, this.mBoardCode, this.mDvachUriBuilder);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription(""), "YouTube");
        assertEquals(info.getSourceUrl(this.mSettings), "http://www.youtube.com/v/dQw4w9WgXcQ");
        assertEquals(info.getThumbnailUrl(), "http://img.youtube.com/vi/dQw4w9WgXcQ/default.jpg");
        //Проверю и те поля, которые не должны вызываться
        assertEquals(info.getSourceExtension(), null);
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_4x);
    }

    public void testNonImageAttachment() {
        IAttachmentEntity entity = new MockAttachmentEntity("src/123.mp3", null, 9000, "");
        AttachmentInfo info = new AttachmentInfo(entity, this.mBoardCode, this.mDvachUriBuilder);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription("Kb"), "9000Kb");
        assertEquals(info.getSourceUrl(this.mSettings), "http://2ch.hk/test/src/123.mp3");
        assertEquals(info.getThumbnailUrl(), null);
        assertEquals(info.getSourceExtension(), "mp3");
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_sound_4x);
    }

    public void testImageAttachment() {
        IAttachmentEntity entity = new MockAttachmentEntity("src/123.jpg", "thumb/123s.jpg", 9000, "");
        AttachmentInfo info = new AttachmentInfo(entity, this.mBoardCode, this.mDvachUriBuilder);

        assertFalse(info.isEmpty());
        assertEquals(info.getDescription("Kb"), "9000Kb");
        assertEquals(info.getSourceUrl(this.mSettings), "http://2ch.hk/test/src/123.jpg");
        assertEquals(info.getThumbnailUrl(), "http://2ch.hk/test/thumb/123s.jpg");
        assertEquals(info.getSourceExtension(), "jpg");
        assertEquals(info.getDefaultThumbnail(), com.vortexwolf.chan.R.drawable.page_white_4x);
    }
}
