package com.lemon95.ymtv.view.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lemon95.androidtvwidget.bridge.EffectNoDrawBridge;
import com.lemon95.androidtvwidget.bridge.OpenEffectBridge;
import com.lemon95.androidtvwidget.view.MainUpView;
import com.lemon95.ymtv.R;
import com.lemon95.ymtv.adapter.FavoritesAdapter;
import com.lemon95.ymtv.adapter.HistoryAdapter;
import com.lemon95.ymtv.bean.FavoritesBean;
import com.lemon95.ymtv.bean.QueryConditions;
import com.lemon95.ymtv.bean.WatchHistories;
import com.lemon95.ymtv.common.AppConstant;
import com.lemon95.ymtv.db.DataBaseDao;
import com.lemon95.ymtv.myview.ConfirmDialog;
import com.lemon95.ymtv.presenter.FavoritesPresenter;
import com.lemon95.ymtv.presenter.HistoryPresenter;
import com.lemon95.ymtv.utils.AppSystemUtils;
import com.lemon95.ymtv.utils.LogUtils;
import com.lemon95.ymtv.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity{

    private GridView lemon_gridview;
    private MainUpView mainUpView1;
    private TextView lemon_msg;
    private HistoryPresenter favoritesPresenter = new HistoryPresenter(this);
    private ProgressBar lemon_movie_details_pro;
    private HistoryAdapter favoritesAdapter;
    private View mOldView;
    public List<WatchHistories.Data> videoList = new ArrayList<>();
    private boolean isDelete = true;
    OpenEffectBridge mOpenEffectBridge;
    public int page = 1;
    private boolean isPage = true; //是否在翻页
    public String mac;
    public String userId;
    List<WatchHistories.Data> dataList;
    private boolean isStart = false;
    private DataBaseDao dataBaseDao;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_history2;
    }

    @Override
    protected void setupViews() {
        dataBaseDao = new DataBaseDao(this);
        lemon_gridview = (GridView)findViewById(R.id.lemon_gridview);
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
        lemon_msg = (TextView) findViewById(R.id.lemon_msg);
        lemon_movie_details_pro = (ProgressBar) findViewById(R.id.lemon_movie_details_pro);
        // 建议使用 NoDraw.
        mainUpView1.setEffectBridge(new EffectNoDrawBridge());
        mOpenEffectBridge = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
        mOpenEffectBridge.setTranDurAnimTime(20);
        // 移动方框缩小的距离.
        mainUpView1.setDrawUpRectPadding(new Rect(10, -10, 4, -43));
        lemon_gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        favoritesAdapter = new HistoryAdapter(videoList,context);
        lemon_gridview.setAdapter(favoritesAdapter);
        mainUpView1.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        lemon_gridview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * 这里注意要加判断是否为NULL.
                 * 因为在重新加载数据以后会出问题.
                 */
                LogUtils.i(TAG, "焦点改变");
                if(19 < Build.VERSION.SDK_INT){
                    isStart = true;
                }
                if (view != null && isStart && view != mOldView) {
                    view.bringToFront();
                    mainUpView1.setFocusView(view, mOldView, 1.1f);
                }
                mOldView = view;
                isStart = true;
                int size = videoList.size();
                if (size - 15 < position && dataList != null && dataList.size() == Integer.parseInt(AppConstant.PAGESIZE)) {
                    if (isPage) {
                        //翻页
                        LogUtils.i(TAG,"翻页");
                        page = page + 1;
                        favoritesPresenter.getFavorites(mac, userId, page);
                        isPage = false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        lemon_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WatchHistories.Data video = videoList.get(position);
                if (AppConstant.FUNNY.equals(video.getVideoTypeId())) {
                    //搞笑
                    Bundle bundle = new Bundle();
                    bundle.putString("videoId", video.getVideoId());
                    bundle.putString("SerialEpisodeId", "");
                    bundle.putString("videoName", video.getTitle());
                    bundle.putString("videoType", video.getVideoTypeId());
                    startActivity(BdPalyActivity.class, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("videoId", video.getVideoId());
                    bundle.putString("videoType", video.getVideoTypeId());
                    startActivity(MovieDetailsActivity.class, bundle);
                }
            }
        });
        lemon_gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final WatchHistories.Data video = videoList.get(position);
                ConfirmDialog.Builder dialog = new ConfirmDialog.Builder(HistoryActivity.this);
                dialog.setMessage(video.getTitle());
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("删除该片", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        favoritesPresenter.deleteVideo(video.getId());
                        dataBaseDao.deleteMoviesLogByMovieId(video.getVideoId());
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
                return true;
            }
        });
        lemon_gridview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                if (lemon_gridview.getChildCount() > 0) {
                    // int v1 = lemon_gridview.getSelectedItemPosition();
                    if (isDelete) {
                        lemon_gridview.setSelection(0);
                        View newView = lemon_gridview.getChildAt(0);
                        newView.bringToFront();
                        mainUpView1.setFocusView(newView, 1.1f);
                        mOldView = lemon_gridview.getChildAt(0);
                        isDelete = false;
                    }
                }
            }
        });
        lemon_gridview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (lemon_gridview.getChildCount() > 0) {
                        // int v1 = lemon_gridview.getSelectedItemPosition();
                        // 设置移动边框的图片.
                        mainUpView1.setUpRectResource(R.drawable.health_focus_border);
                        lemon_gridview.setSelection(0);
                        View newView = lemon_gridview.getChildAt(0);
                        newView.bringToFront();
                        mainUpView1.setFocusView(newView, 1.1f);
                        mOldView = lemon_gridview.getChildAt(0);
                    }
                    LogUtils.i(TAG,"gridView 获取焦点");
                }
            }
        });
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    protected void initialized() {
        //获取收藏数据
        showPro();
        mac = AppSystemUtils.getDeviceId();
        userId = PreferenceUtils.getString(context, AppConstant.USERID, "");
        favoritesPresenter.getFavorites(mac,userId,page);
    }

    public void showPro() {
        lemon_movie_details_pro.setVisibility(View.VISIBLE);
        lemon_msg.setVisibility(View.GONE);
        lemon_gridview.setVisibility(View.GONE);
    }

    public void hidePro() {
        lemon_movie_details_pro.setVisibility(View.GONE);
        lemon_msg.setVisibility(View.VISIBLE);
        lemon_gridview.setVisibility(View.VISIBLE);
    }

    public void showError(String msg) {
        mainUpView1.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        lemon_msg.setText(msg);
        lemon_msg.setVisibility(View.VISIBLE);
        lemon_gridview.setVisibility(View.GONE);
        lemon_movie_details_pro.setVisibility(View.GONE);
    }

    //初始化观看记录数据
    public void showFavoriteData(List<WatchHistories.Data> dataList) {
        isPage = true;
        this.dataList = dataList;
        if (dataList != null) {
            videoList.addAll(dataList);
            favoritesAdapter.notifyDataSetChanged();
            hidePro();
        }
    }


}
