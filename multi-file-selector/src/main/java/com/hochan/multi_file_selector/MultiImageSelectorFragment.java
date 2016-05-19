package com.hochan.multi_file_selector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.hochan.multi_file_selector.adapter.AudioAdapter;
import com.hochan.multi_file_selector.adapter.FolderAdapter;
import com.hochan.multi_file_selector.adapter.ImageAdapter;
import com.hochan.multi_file_selector.data.AudioFile;
import com.hochan.multi_file_selector.data.Folder;
import com.hochan.multi_file_selector.data.MediaFile;
import com.hochan.multi_file_selector.listener.MediaFileAdapterListener;
import com.hochan.multi_file_selector.loader.DataLoader;
import com.hochan.multi_file_selector.tool.ScreenTools;
import com.hochan.multi_file_selector.view.RecycleViewDivider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/14.
 */
public class MultiImageSelectorFragment extends Fragment
        implements DataLoader.DataLoaderCallBack, MediaFileAdapterListener{

    public static final String TAG = "multi_image_selector";
    public static final String EXTRA_RESULT = "extra_result";

    private Context mContext;
    private DataLoader mLoaderCallback = null;

    private ImageAdapter mImageAdapter;
    private static int mImageColumn = 3;
    private static int mImageGap = 3;

    private AudioAdapter mAudioAdapter;
    private FolderAdapter mFolderAdapter;
    private int mCurrentAlbum = 0;
    private int mSelectType;

    //view
    private RecyclerView rclvMediaFiles;
    private Button btnFolders, btnOpera, btnArtists;
    private ListPopupWindow mFolderPopupWindow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_image_selector, container, false);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();

        mSelectType = getArguments().getInt(MultiFileSelectorActivity.TYPE_SELECT);
        System.out.println("选择文件类型："+mSelectType);

        rclvMediaFiles = (RecyclerView) view.findViewById(R.id.rclv_images);
        btnArtists = (Button) view.findViewById(R.id.btn_artists);
        btnFolders = (Button) view.findViewById(R.id.btn_folders);
        btnOpera = (Button) view.findViewById(R.id.btn_opera);

        btnOpera.setText("所有"+MediaFile.TYPE_NAME.get(mSelectType));

        mFolderAdapter = new FolderAdapter(mContext);

        switch (mSelectType){
            case MediaFile.TYPE_IMAGE:
                btnArtists.setVisibility(View.GONE);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, mImageColumn);
                rclvMediaFiles.setLayoutManager(gridLayoutManager);
                rclvMediaFiles.addItemDecoration(new RecycleViewDivider(mContext,
                        RecycleViewDivider.ORIENTATION_BOTH, ScreenTools.dip2px(mContext, mImageGap), 0));

                mImageAdapter = new ImageAdapter(mContext, mImageColumn);
                mImageAdapter.setImageAdpaterListener(this);
                rclvMediaFiles.setAdapter(mImageAdapter);
                rclvMediaFiles.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if(newState == RecyclerView.SCROLL_STATE_SETTLING)
                            Picasso.with(mContext).pauseTag(TAG);
                        else
                            Picasso.with(mContext).resumeTag(TAG);
                    }
                });
                break;
            case MediaFile.TYPE_AUDIO:
                btnArtists.setVisibility(View.VISIBLE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                rclvMediaFiles.setLayoutManager(linearLayoutManager);

                mAudioAdapter = new AudioAdapter(mContext);
                mAudioAdapter.setmAdapterListener(this);
                rclvMediaFiles.setAdapter(mAudioAdapter);
                break;
            case MediaFile.TYPE_VIDEO:
                break;
        }

        btnFolders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFolderPopupWindow == null)
                    createFolderPopupWindow();
                if(mFolderPopupWindow.isShowing()){
                    mFolderPopupWindow.dismiss();
                }else{
                    mFolderPopupWindow.show();
                    int index = mCurrentAlbum == 0 ? 0 : mCurrentAlbum-1;
                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        btnOpera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBackImages();
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderCallback = new DataLoader(getActivity(), mSelectType);
        mLoaderCallback.setCallBack(this);
        getActivity().getSupportLoaderManager().initLoader(
                mSelectType, null, mLoaderCallback);
    }

    private void createFolderPopupWindow(){
        mFolderPopupWindow = new ListPopupWindow(mContext);
        mFolderPopupWindow.setContentWidth(ScreenTools.getScreenWidth(mContext));
        mFolderPopupWindow.setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.colorBackground)));
        mFolderPopupWindow.setWidth(ScreenTools.getScreenWidth(mContext));
        mFolderPopupWindow.setHeight(ScreenTools.getScreenWidth(mContext));
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setAnchorView(btnFolders);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFolderPopupWindow.dismiss();
                mImageAdapter.setData((mFolderAdapter.getItem(position)).getmMediaFiles());
                mCurrentAlbum = position;
                btnFolders.setText(mFolderAdapter.getItem(position).getmName());
            }
        });
    }

    @Override
    public void finish(List<MediaFile> mediaFiles, List<Folder> folders) {
        switch (mSelectType) {
            case MediaFile.TYPE_IMAGE:
                mImageAdapter.setData((ArrayList<MediaFile>) mediaFiles);
                Folder folder = new Folder(MediaFile.TYPE_IMAGE, "所有图片", null, mediaFiles);
                folders.add(0, folder);
                mFolderAdapter.setData(folders);
                System.out.println(mediaFiles.size());
                break;
            case MediaFile.TYPE_AUDIO:
                mAudioAdapter.setData(mediaFiles);
                Folder audioFolder = new Folder(MediaFile.TYPE_AUDIO, "所有音乐", null, mediaFiles);
                folders.add(0, audioFolder);
                mFolderAdapter.setData(folders);
                System.out.println(mediaFiles.size());
                break;
            default:
                System.out.println("mSelectType:"+mSelectType);
                break;
        }
    }

    @Override
    public void fileSelected(int selectedCount) {
        if(selectedCount > 0)
            btnOpera.setText(String.format("完成(%s)", Integer.valueOf(selectedCount)));
        else
            btnOpera.setText("取消");
    }

    private void sendBackImages(){
        if(mImageAdapter.getSelectedImages().size() == 0)
            getActivity().finish();
        else{
            ArrayList<String> resultList = new ArrayList<>();
            for(MediaFile mediaFile : mImageAdapter.getSelectedImages()){
                resultList.add(mediaFile.getmPath());
            }
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }
}
