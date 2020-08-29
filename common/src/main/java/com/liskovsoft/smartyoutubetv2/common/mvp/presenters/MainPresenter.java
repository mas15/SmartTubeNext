package com.liskovsoft.smartyoutubetv2.common.mvp.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import com.liskovsoft.mediaserviceinterfaces.MediaGroupManager;
import com.liskovsoft.mediaserviceinterfaces.MediaService;
import com.liskovsoft.mediaserviceinterfaces.SignInManager;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.mvp.models.Header;
import com.liskovsoft.smartyoutubetv2.common.mvp.models.Video;
import com.liskovsoft.smartyoutubetv2.common.mvp.models.VideoGroup;
import com.liskovsoft.smartyoutubetv2.common.mvp.views.MainView;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs;
import com.liskovsoft.youtubeapi.service.YouTubeMediaService;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainPresenter implements Presenter<MainView> {
    private static final String TAG = MainPresenter.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static MainPresenter sInstance;
    private final Handler mHandler = new Handler();
    private final Context mContext;
    private final ArrayList<MediaGroup> mMediaGroups;
    private final Map<Integer, Header> mHeaders = new HashMap<>();
    private final PlaybackPresenter mPlaybackPresenter;
    private final MediaService mMediaService;
    private MainView mView;

    private MainPresenter(Context context) {
        mMediaGroups = new ArrayList<>();
        mContext = context;
        mPlaybackPresenter = PlaybackPresenter.instance(context);
        mMediaService = YouTubeMediaService.instance();
        GlobalPreferences.instance(context);
    }

    public static MainPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new MainPresenter(context.getApplicationContext());
        }

        return sInstance;
    }

    @Override
    public void onInitDone() {
        if (mView == null) {
            return;
        }

        if (!AppPrefs.instance(mContext).getCompletedOnboarding()) {
            // This is the first time running the app, let's go to onboarding
            mView.showOnboarding();
        }


        checkUserIsSigned();
        initHeaders();
        loadHomeData();
        loadSubscriptions();
        loadHistory();
    }

    @Override
    public void register(MainView view) {
        mView = view;
    }

    @Override
    public void unregister(MainView view) {
        mView = null;
    }

    public void onVideoItemClicked(Video item) {
        if (mView == null) {
            return;
        }

        mPlaybackPresenter.setVideo(item);
        mView.openPlaybackView();
    }

    public void onVideoItemLongPressed(Video item) {
        if (mView == null) {
            return;
        }

        mView.openDetailsView(item);
    }

    private void initHeaders() {
        mHeaders.put(MediaGroup.TYPE_HOME, new Header(MediaGroup.TYPE_HOME, mContext.getString(R.string.header_home), Header.TYPE_ROW));
        mHeaders.put(MediaGroup.TYPE_SEARCH, new Header(MediaGroup.TYPE_SEARCH, mContext.getString(R.string.header_search)));
        mHeaders.put(MediaGroup.TYPE_SUBSCRIPTIONS, new Header(MediaGroup.TYPE_SUBSCRIPTIONS, mContext.getString(R.string.header_subscriptions)));
        mHeaders.put(MediaGroup.TYPE_HISTORY, new Header(MediaGroup.TYPE_HISTORY, mContext.getString(R.string.header_history)));

        mView.updateHeader(null, mHeaders.get(MediaGroup.TYPE_HOME));
        mView.updateHeader(null, mHeaders.get(MediaGroup.TYPE_SUBSCRIPTIONS));
        mView.updateHeader(null, mHeaders.get(MediaGroup.TYPE_HISTORY));
    }

    // TODO: implement Android TV channels
    //private void updateRecommendations() {
    //    Intent recommendationIntent = new Intent(mContext, UpdateRecommendationsService.class);
    //    mContext.startService(recommendationIntent);
    //}

    @SuppressLint("CheckResult")
    private void checkUserIsSigned() {
        SignInManager signInManager = mMediaService.getSignInManager();
        if (!signInManager.isSigned()) {
            signInManager.signInObserve()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe((userCode) -> Log.d(TAG, "User code is: " + userCode), error -> Log.e(TAG, error));
        }
    }

    @SuppressLint("CheckResult")
    private void loadHomeData() {
        MediaGroupManager mediaGroupManager = mMediaService.getMediaGroupManager();

        mediaGroupManager.getHomeObserve()
                .subscribeOn(Schedulers.newThread())
                .subscribe(mediaGroups -> {

            if (mediaGroups == null) {
                Log.e(TAG, "Home groups not found");
                return;
            }

            for (MediaGroup mediaGroup : mediaGroups) {
                if (mediaGroup.getMediaItems() == null) {
                    Log.e(TAG, "MediaGroup is empty: " + mediaGroup.getTitle());
                    continue;
                }

                mView.updateHeader(VideoGroup.from(mediaGroup), mHeaders.get(MediaGroup.TYPE_HOME));

                mMediaGroups.add(mediaGroup);
            }
        },
        error -> Log.e(TAG, error),
        () -> {
            // continue nested groups

            // TODO: How many times group should be continued? Maybe continue on demand?
            for (MediaGroup mediaGroup : mMediaGroups) {
                mediaGroupManager.continueGroupObserve(mediaGroup)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(continueMediaGroup -> {
                            if (continueMediaGroup == null) {
                                Log.e(TAG, "Next Home group is empty: " + mediaGroup.getTitle());
                                return;
                            }

                            mView.updateHeader(VideoGroup.from(continueMediaGroup), mHeaders.get(MediaGroup.TYPE_HOME));
                        },
                        error -> Log.e(TAG, error));
            }
        });
    }

    @SuppressLint("CheckResult")
    private void loadSearchData() {
        MediaGroupManager mediaGroupManager = mMediaService.getMediaGroupManager();

        mediaGroupManager.getSearchObserve("Самый лучший фильм")
                .subscribeOn(Schedulers.newThread())
                .subscribe(mediaGroup -> {
                    mView.updateHeader(VideoGroup.from(mediaGroup), mHeaders.get(MediaGroup.TYPE_SEARCH));
                }, error -> Log.e(TAG, error));
    }

    @SuppressLint("CheckResult")
    private void loadSubscriptions() {
        MediaGroupManager mediaGroupManager = mMediaService.getMediaGroupManager();

        mediaGroupManager.getSubscriptionsObserve()
                .subscribeOn(Schedulers.newThread())
                .subscribe(mediaGroup -> {
                    if (mediaGroup == null) {
                        Log.e(TAG, "Can't obtain subscriptions. User probably not logged in");
                        return;
                    }

                    mView.updateHeader(VideoGroup.from(mediaGroup), mHeaders.get(MediaGroup.TYPE_SUBSCRIPTIONS));
                }, error -> Log.e(TAG, error));
    }

    @SuppressLint("CheckResult")
    private void loadHistory() {
        MediaGroupManager mediaGroupManager = mMediaService.getMediaGroupManager();

        mediaGroupManager.getHistoryObserve()
                .subscribeOn(Schedulers.newThread())
                .subscribe(mediaGroup -> {
                    if (mediaGroup == null) {
                        Log.e(TAG, "Can't obtain history. User probably not logged in");
                        return;
                    }

                    mView.updateHeader(VideoGroup.from(mediaGroup), mHeaders.get(MediaGroup.TYPE_HISTORY));
                }, error -> Log.e(TAG, error));
    }
}
