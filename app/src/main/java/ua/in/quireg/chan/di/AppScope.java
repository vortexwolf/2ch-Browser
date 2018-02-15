package ua.in.quireg.chan.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

import dagger.releasablereferences.CanReleaseReferences;

/**
 * Created by Arcturus Mengsk on 12/5/2017, 2:22 AM.
 * 2ch-Browser
 */

@Scope
@Retention(value= RetentionPolicy.RUNTIME)
@CanReleaseReferences
public @interface AppScope {

}
